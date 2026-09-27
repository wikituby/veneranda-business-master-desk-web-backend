package org.example.finance.incomes.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class IncomeEntry extends PanacheEntity {

  @ManyToOne
  @JoinColumn(name = "income_account_id")
  public IncomeAccount incomeAccount;

  @Column
  public String incomeAccountType;

  @Column
  public String incomeAccountName;

  @Column
  public String incomeCategoryName;

  @Column(nullable = false)
  public BigDecimal amount;

  @Column
  @JsonbDateFormat(value = "yyyy/MM/dd")
  public LocalDate incomeDate;

  @Column
  public Long incomeSourceId;

  @Column
  public String sourceName;

  @Column
  public Long incomePaymentMethodId;

  @Column
  public String paymentMethod;

  /** Auto-generated internal transaction reference. */
  @Column
  public String referenceCode;

  /** External reference from receipt when electronic. */
  @Column
  public String referenceNumber;

  @Column
  public Boolean electronicReceipt;

  @Column(columnDefinition = "TEXT")
  public String description;

  @Column(name = "user_id")
  public Long recordedByUserId;

  @Column
  public String recordedByUserName;
}
