package org.example.finance.loans.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.invoice.services.InvoiceService;
import org.example.finance.loans.domains.Loan;
import org.example.finance.loans.domains.repositories.LoanRepository;
import org.example.finance.loans.services.payloads.requests.LoanRepaymentRequest;
import org.example.finance.loans.services.payloads.requests.LoanRequest;
import org.example.finance.loans.services.payloads.responses.LoanDto;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@ApplicationScoped
public class LoanService {

    @Inject
    LoanRepository loanRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    InvoiceService invoiceService;

    @Transactional
    public Response createLoan(LoanRequest request) {
        if (request == null || request.borrowerName == null || request.borrowerName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Borrower name is required.", null))
                    .build();
        }
        if (request.principalAmount == null || request.principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter a valid principal amount.", null))
                    .build();
        }
        if (request.recordedByUserId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("User session not found.", null))
                    .build();
        }

        User user = userRepository.findById(request.recordedByUserId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("User not found for ID.", request.recordedByUserId))
                    .build();
        }
        Loan loan = new Loan();
        loan.recordedByUserId = user.id;
        loan.recordedByUserName = user.username;
        loan.borrowerName = request.borrowerName.trim();
        loan.borrowerContact = trimOrEmpty(request.borrowerContact);
        loan.borrowerType = normalizeBorrowerType(request.borrowerType);
        loan.principalAmount = request.principalAmount;
        loan.amountRepaid = BigDecimal.ZERO;
        loan.loanDate = parseDateOrToday(request.loanDate);
        loan.dueDate = parseDateOrNull(request.dueDate);
        loan.description = trimOrEmpty(request.description);
        loan.referenceNumber = invoiceService.generateRandomReferenceNo(12);
        loan.status = resolveStatus(loan);
        loan.repaymentNotes = "";

        loanRepository.persist(loan);
        return Response.ok(new ResponseMessage("Loan recorded successfully", new LoanDto(loan))).build();
    }

    @Transactional
    public List<LoanDto> getAllLoans() {
        return loanRepository.listAll(Sort.descending("id"))
                .stream()
                .map(LoanDto::new)
                .toList();
    }

    @Transactional
    public Response updateLoan(Long id, LoanRequest request) {
        Loan loan = loanRepository.findById(id);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Loan not found", null))
                    .build();
        }
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invalid request", null))
                    .build();
        }
        if (request.borrowerName == null || request.borrowerName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Borrower name is required.", null))
                    .build();
        }
        if (request.principalAmount == null || request.principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter a valid principal amount.", null))
                    .build();
        }
        BigDecimal repaid = loan.amountRepaid != null ? loan.amountRepaid : BigDecimal.ZERO;
        if (request.principalAmount.compareTo(repaid) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Principal cannot be less than amount already repaid.", null))
                    .build();
        }

        loan.borrowerName = request.borrowerName.trim();
        loan.borrowerContact = trimOrEmpty(request.borrowerContact);
        loan.borrowerType = normalizeBorrowerType(request.borrowerType);
        loan.principalAmount = request.principalAmount;
        loan.loanDate = parseDateOrToday(request.loanDate);
        loan.dueDate = parseDateOrNull(request.dueDate);
        loan.description = trimOrEmpty(request.description);
        loan.status = resolveStatus(loan);

        loanRepository.persist(loan);
        return Response.ok(new ResponseMessage("Loan updated successfully", new LoanDto(loan))).build();
    }

    @Transactional
    public Response recordRepayment(Long id, LoanRepaymentRequest request) {
        Loan loan = loanRepository.findById(id);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Loan not found", null))
                    .build();
        }
        if (request == null || request.repaymentAmount == null || request.repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter a valid repayment amount.", null))
                    .build();
        }

        BigDecimal repaid = loan.amountRepaid != null ? loan.amountRepaid : BigDecimal.ZERO;
        BigDecimal balance = balanceOutstanding(loan);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("This loan is already fully repaid.", null))
                    .build();
        }
        if (request.repaymentAmount.compareTo(balance) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Repayment exceeds outstanding balance.", balance))
                    .build();
        }

        loan.amountRepaid = repaid.add(request.repaymentAmount);
        if (request.note != null && !request.note.isBlank()) {
            String entry = LocalDate.now() + ": " + request.repaymentAmount + " — " + request.note.trim();
            loan.repaymentNotes = loan.repaymentNotes == null || loan.repaymentNotes.isBlank()
                    ? entry
                    : loan.repaymentNotes + "\n" + entry;
        }
        loan.status = resolveStatus(loan);
        loanRepository.persist(loan);
        return Response.ok(new ResponseMessage("Repayment recorded successfully", new LoanDto(loan))).build();
    }

    @Transactional
    public Response deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id);
        if (loan == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Loan not found", null))
                    .build();
        }
        loanRepository.delete(loan);
        return Response.ok(new ResponseMessage("Loan deleted successfully")).build();
    }

    public static BigDecimal balanceOutstanding(Loan loan) {
        if (loan == null || loan.principalAmount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal repaid = loan.amountRepaid != null ? loan.amountRepaid : BigDecimal.ZERO;
        BigDecimal balance = loan.principalAmount.subtract(repaid);
        return balance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : balance;
    }

    public static String resolveStatus(Loan loan) {
        if (loan == null) {
            return "ACTIVE";
        }
        if (balanceOutstanding(loan).compareTo(BigDecimal.ZERO) <= 0) {
            return "PAID";
        }
        if (loan.dueDate != null && loan.dueDate.isBefore(LocalDate.now())) {
            return "OVERDUE";
        }
        return "ACTIVE";
    }

    private LocalDate parseDateOrToday(String value) {
        LocalDate parsed = parseDateOrNull(value);
        return parsed != null ? parsed : LocalDate.now();
    }

    private LocalDate parseDateOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(value.trim().replace('/', '-'));
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }

    private String normalizeBorrowerType(String value) {
        if (value == null || value.isBlank()) {
            return "OTHER";
        }
        return value.trim().toUpperCase();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
