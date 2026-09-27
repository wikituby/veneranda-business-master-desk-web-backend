package org.example.finance.incomes.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.incomes.domains.IncomeAccount;
import org.example.finance.incomes.domains.IncomeEntry;
import org.example.finance.incomes.domains.IncomePaymentMethod;
import org.example.finance.incomes.domains.IncomeSource;
import org.example.finance.incomes.domains.repositories.IncomeAccountRepository;
import org.example.finance.incomes.domains.repositories.IncomeEntryRepository;
import org.example.finance.incomes.domains.repositories.IncomePaymentMethodRepository;
import org.example.finance.incomes.domains.repositories.IncomeSourceRepository;
import org.example.finance.incomes.services.payloads.requests.IncomeEntryRequest;
import org.example.finance.incomes.services.payloads.responses.IncomeAccountSummaryDto;
import org.example.finance.incomes.services.payloads.responses.IncomeDto;
import org.example.finance.incomes.services.payloads.responses.IncomeSummaryDto;
import org.example.finance.invoice.services.InvoiceService;
import org.example.finance.payments.cash.domains.Payments;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class IncomeService {

    public static final String CLIENT_PAYMENTS = "CLIENT_PAYMENTS";
    public static final String DONATIONS = "DONATIONS";
    public static final String INVESTORS = "INVESTORS";
    public static final String GRANTS = "GRANTS";
    public static final String OTHER = "OTHER";

    private static final List<String> LEGACY_ACCOUNT_TYPES = List.of(DONATIONS, INVESTORS, GRANTS, OTHER);

    @Inject
    IncomeEntryRepository incomeEntryRepository;

    @Inject
    IncomeAccountRepository incomeAccountRepository;

    @Inject
    IncomePaymentMethodRepository incomePaymentMethodRepository;

    @Inject
    IncomeSourceRepository incomeSourceRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    InvoiceService invoiceService;

    @Transactional
    public Response createIncomeEntry(IncomeEntryRequest request) {
        if (request == null || request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter a valid income amount.", null))
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

        IncomeEntry entry = new IncomeEntry();
        if (!applyIncomeAccount(entry, request)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Select a valid income account.", null))
                    .build();
        }

        entry.amount = request.amount;
        entry.incomeDate = parseDateOrToday(request.incomeDate);
        applySource(entry, request);
        applyPaymentMethod(entry, request);
        entry.referenceCode = generateReferenceCode();
        entry.electronicReceipt = request.electronicReceipt != null && request.electronicReceipt;
        entry.referenceNumber = trimOrEmpty(request.referenceNumber);
        if (entry.electronicReceipt && entry.referenceNumber.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter the receipt reference for electronic payments.", null))
                    .build();
        }
        entry.description = trimOrEmpty(request.description);
        entry.recordedByUserId = user.id;
        entry.recordedByUserName = user.username;

        incomeEntryRepository.persist(entry);
        return Response.ok(new ResponseMessage("Income recorded successfully", new IncomeDto(entry))).build();
    }

    @Transactional
    public List<IncomeDto> getAllIncomeEntries(String accountType, String dateFrom, String dateTo) {
        LocalDate from = parseDateOrNull(dateFrom);
        LocalDate to = parseDateOrNull(dateTo);
        String normalizedAccount = normalizeLegacyAccountType(accountType);

        List<IncomeEntry> entries;
        if (normalizedAccount != null && from != null && to != null) {
            entries = incomeEntryRepository.list(
                    "incomeAccountType = ?1 and incomeDate >= ?2 and incomeDate <= ?3",
                    Sort.descending("id"),
                    normalizedAccount, from, to);
        } else if (normalizedAccount != null) {
            entries = incomeEntryRepository.list("incomeAccountType = ?1", Sort.descending("id"), normalizedAccount);
        } else if (from != null && to != null) {
            entries = incomeEntryRepository.list(
                    "incomeDate >= ?1 and incomeDate <= ?2",
                    Sort.descending("id"),
                    from, to);
        } else {
            entries = incomeEntryRepository.listAll(Sort.descending("id"));
        }

        return entries.stream().map(IncomeDto::new).toList();
    }

    @Transactional
    public IncomeSummaryDto getIncomeSummary(String dateFrom, String dateTo) {
        LocalDate from = parseDateOrNull(dateFrom);
        LocalDate to = parseDateOrNull(dateTo);

        IncomeSummaryDto summary = new IncomeSummaryDto();
        summary.dateFrom = dateFrom;
        summary.dateTo = dateTo;

        Map<String, IncomeAccountSummaryDto> byAccount = new LinkedHashMap<>();

        BigDecimal clientTotal = sumClientPayments(from, to);
        long clientCount = countClientPayments(from, to);
        byAccount.put(CLIENT_PAYMENTS, new IncomeAccountSummaryDto(
                CLIENT_PAYMENTS, accountLabel(CLIENT_PAYMENTS), clientTotal, clientCount, true));

        for (IncomeAccount account : incomeAccountRepository.listAll(Sort.ascending("accountName"))) {
            String key = "ACCOUNT_" + account.id;
            byAccount.put(key, new IncomeAccountSummaryDto(
                    key,
                    account.accountName,
                    BigDecimal.ZERO,
                    0,
                    false));
        }

        for (String accountType : LEGACY_ACCOUNT_TYPES) {
            if (!byAccount.containsKey(accountType)) {
                byAccount.put(accountType, new IncomeAccountSummaryDto(
                        accountType, accountLabel(accountType), BigDecimal.ZERO, 0, false));
            }
        }

        List<IncomeEntry> entries = listEntriesInRange(from, to);
        for (IncomeEntry entry : entries) {
            String key = accountKeyFor(entry);
            IncomeAccountSummaryDto row = byAccount.get(key);
            if (row == null) {
                row = new IncomeAccountSummaryDto(key, labelForEntry(entry), BigDecimal.ZERO, 0, false);
                byAccount.put(key, row);
            }
            BigDecimal amount = entry.amount != null ? entry.amount : BigDecimal.ZERO;
            row.totalAmount = row.totalAmount.add(amount);
            row.entryCount += 1;
        }

        summary.accounts = new ArrayList<>(byAccount.values());
        summary.totalIncome = summary.accounts.stream()
                .map(a -> a.totalAmount != null ? a.totalAmount : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return summary;
    }

    @Transactional
    public Response updateIncomeEntry(Long id, IncomeEntryRequest request) {
        IncomeEntry entry = incomeEntryRepository.findById(id);
        if (entry == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income entry not found", null))
                    .build();
        }
        if (request == null || request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter a valid income amount.", null))
                    .build();
        }
        if (!applyIncomeAccount(entry, request)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Select a valid income account.", null))
                    .build();
        }

        entry.amount = request.amount;
        entry.incomeDate = parseDateOrToday(request.incomeDate);
        applySource(entry, request);
        applyPaymentMethod(entry, request);
        entry.electronicReceipt = request.electronicReceipt != null && request.electronicReceipt;
        entry.referenceNumber = trimOrEmpty(request.referenceNumber);
        if (entry.electronicReceipt && entry.referenceNumber.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Enter the receipt reference for electronic payments.", null))
                    .build();
        }
        entry.description = trimOrEmpty(request.description);

        incomeEntryRepository.persist(entry);
        return Response.ok(new ResponseMessage("Income updated successfully", new IncomeDto(entry))).build();
    }

    @Transactional
    public Response deleteIncomeEntry(Long id) {
        IncomeEntry entry = incomeEntryRepository.findById(id);
        if (entry == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Income entry not found", null))
                    .build();
        }
        incomeEntryRepository.delete(entry);
        return Response.ok(new ResponseMessage("Income entry deleted successfully")).build();
    }

    public static String accountLabel(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return "Other";
        }
        return switch (accountType.trim().toUpperCase(Locale.ROOT)) {
            case CLIENT_PAYMENTS -> "Payments from Clients";
            case DONATIONS -> "Donations";
            case INVESTORS -> "Investors";
            case GRANTS -> "Grants & Subsidies";
            case OTHER -> "Other Income";
            default -> accountType;
        };
    }

    private boolean applyIncomeAccount(IncomeEntry entry, IncomeEntryRequest request) {
        if (request.incomeAccountId != null) {
            IncomeAccount account = incomeAccountRepository.findById(request.incomeAccountId);
            if (account == null) {
                return false;
            }
            entry.incomeAccount = account;
            entry.incomeAccountName = account.accountName;
            entry.incomeCategoryName = account.incomeCategoryName;
            entry.incomeAccountType = "ACCOUNT_" + account.id;
            return true;
        }
        String accountType = normalizeLegacyAccountType(request.incomeAccountType);
        if (accountType == null) {
            return false;
        }
        entry.incomeAccount = null;
        entry.incomeAccountType = accountType;
        entry.incomeAccountName = accountLabel(accountType);
        entry.incomeCategoryName = accountLabel(accountType);
        return true;
    }

    private void applySource(IncomeEntry entry, IncomeEntryRequest request) {
        if (request.incomeSourceId != null) {
            IncomeSource source = incomeSourceRepository.findById(request.incomeSourceId);
            if (source != null) {
                entry.incomeSourceId = source.id;
                entry.sourceName = source.sourceName;
                return;
            }
        }
        entry.incomeSourceId = request.incomeSourceId;
        entry.sourceName = trimOrEmpty(request.sourceName);
    }

    private void applyPaymentMethod(IncomeEntry entry, IncomeEntryRequest request) {
        if (request.incomePaymentMethodId != null) {
            IncomePaymentMethod method = incomePaymentMethodRepository.findById(request.incomePaymentMethodId);
            if (method != null) {
                entry.incomePaymentMethodId = method.id;
                entry.paymentMethod = method.methodName;
                return;
            }
        }
        entry.incomePaymentMethodId = request.incomePaymentMethodId;
        entry.paymentMethod = trimOrEmpty(request.paymentMethod);
    }

    private String accountKeyFor(IncomeEntry entry) {
        if (entry.incomeAccount != null) {
            return "ACCOUNT_" + entry.incomeAccount.id;
        }
        if (entry.incomeAccountType != null && entry.incomeAccountType.startsWith("ACCOUNT_")) {
            return entry.incomeAccountType;
        }
        String type = normalizeLegacyAccountType(entry.incomeAccountType);
        return type != null ? type : OTHER;
    }

    private String labelForEntry(IncomeEntry entry) {
        if (entry.incomeAccountName != null && !entry.incomeAccountName.isBlank()) {
            return entry.incomeAccountName;
        }
        return accountLabel(entry.incomeAccountType);
    }

    private String generateReferenceCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "INC-" + datePart + "-" + invoiceService.generateRandomReferenceNo(6);
    }

    private BigDecimal sumClientPayments(LocalDate from, LocalDate to) {
        return listPaymentsInRange(from, to).stream()
                .filter(payment -> !isReversedPayment(payment))
                .map(payment -> payment.amountToPay != null ? payment.amountToPay : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countClientPayments(LocalDate from, LocalDate to) {
        return listPaymentsInRange(from, to).stream()
                .filter(payment -> !isReversedPayment(payment))
                .count();
    }

    private List<Payments> listPaymentsInRange(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return Payments.list("dateOfPayment >= ?1 and dateOfPayment <= ?2", from, to);
        }
        return Payments.listAll();
    }

    private List<IncomeEntry> listEntriesInRange(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return incomeEntryRepository.list("incomeDate >= ?1 and incomeDate <= ?2", from, to);
        }
        return incomeEntryRepository.listAll();
    }

    private boolean isReversedPayment(Payments payment) {
        return payment != null
                && payment.status != null
                && "reversed".equalsIgnoreCase(payment.status.trim());
    }

    private String normalizeLegacyAccountType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (CLIENT_PAYMENTS.equals(normalized) || normalized.startsWith("ACCOUNT_")) {
            return null;
        }
        if (LEGACY_ACCOUNT_TYPES.contains(normalized)) {
            return normalized;
        }
        if ("DONATION".equals(normalized)) {
            return DONATIONS;
        }
        if ("INVESTOR".equals(normalized)) {
            return INVESTORS;
        }
        if ("GRANT".equals(normalized)) {
            return GRANTS;
        }
        return OTHER;
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

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
