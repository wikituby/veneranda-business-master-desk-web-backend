package org.example.finance.expenses.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.expenses.domains.ExpenseAccount;
import org.example.finance.expenses.domains.ExpenseTransaction;
import org.example.finance.expenses.domains.repositories.ExpenseAccountRepository;
import org.example.finance.expenses.domains.repositories.ExpenseTransactionRepository;
import org.example.finance.expenses.services.payloads.requests.ExpenseTransactionRequest;
import org.example.finance.expenses.services.payloads.responses.ExpenseTransactionDto;
import org.example.finance.invoice.services.InvoiceService;
import org.example.user.domains.User;
import org.example.user.domains.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
public class ExpenseTransactionService {

    @Inject
    ExpenseTransactionRepository expenseTransactionRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ExpenseAccountRepository expenseAccountRepository;

    @Inject
    InvoiceService invoiceService;

    public Response createExpenseTransaction(ExpenseTransactionRequest request){

        User user = userRepository.findById(request.userId);
        ExpenseAccount expenseAccount = expenseAccountRepository.findById(request.expenseAccountId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("User not found for ID.", request.userId))
                    .build();
        }

        if (expenseAccount == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("ExpenseAccount not found for ID.", request.expenseAccountId))
                    .build();
        }

        ExpenseTransaction expenseTransaction = new ExpenseTransaction();
        expenseTransaction.user = user;
        expenseTransaction.userName = user.username;
        expenseTransaction.expenseAccount = expenseAccount;
        expenseTransaction.expenseAccountName = expenseAccount.accountName;
        expenseTransaction.amountTransacted = request.amountTransacted;
        expenseTransaction.description = request.description;
        expenseTransaction.receiver = request.receiver;
        expenseTransaction.referenceNumber = invoiceService.generateRandomReferenceNo(20);
        expenseTransaction.dateOfExpenseTransaction = LocalDate.now();
        expenseTransaction.upDateOfExpenseTransaction = LocalDate.now();
        expenseTransaction.timeOfExpenseTransaction = LocalTime.now();

        expenseTransactionRepository.persist(expenseTransaction);

        return Response.ok(new ResponseMessage("New ExpenseTransaction processed successfully", new ExpenseTransactionDto(expenseTransaction))).build();

    }


    @Transactional
    public List<ExpenseTransactionDto> getAllExpenseTransactions() {
        return expenseTransactionRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ExpenseTransactionDto::new)
                .toList();
    }


    @Transactional
    public Response deleteExpenseTransactionById(Long id){
        ExpenseTransaction expenseTransaction = expenseTransactionRepository.findById(id);
        if (expenseTransaction == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Expense Transaction not found", null))
                    .build();
        }
        expenseTransactionRepository.delete(expenseTransaction);
        return Response.ok(new ResponseMessage("Expense Transaction Deleted successfully")).build();
    }

}






