export const emptyBootstrap = {
  currentPeriod: "",
  accounts: [],
  expenseCategories: [],
  incomeCategories: [],
};

export const typeLabels = {
  EXPENSE: "支出",
  INCOME: "收入",
  TRANSFER: "转账",
};

export const frequencyLabels = {
  WEEKLY: "每周",
  MONTHLY: "每月",
};

const currencyFormatter = new Intl.NumberFormat("zh-CN", {
  style: "currency",
  currency: "CNY",
  maximumFractionDigits: 2,
});

const percentFormatter = new Intl.NumberFormat("zh-CN", {
  maximumFractionDigits: 1,
});

export function formatCurrency(value) {
  return currencyFormatter.format(Number(value || 0));
}

export function formatPercent(value) {
  return `${percentFormatter.format(Number(value || 0))}%`;
}

export function trimToNull(value) {
  const normalized = `${value || ""}`.trim();
  return normalized ? normalized : null;
}

export function today() {
  return new Date().toISOString().slice(0, 10);
}

export function nextMonthDay(day = 1) {
  const value = new Date();
  value.setMonth(value.getMonth() + 1);
  value.setDate(day);
  return value.toISOString().slice(0, 10);
}

export function firstOptionId(list) {
  return list?.[0]?.id ? String(list[0].id) : "";
}

export function categoriesByType(bootstrap, type) {
  if (type === "INCOME") {
    return bootstrap.incomeCategories;
  }
  if (type === "EXPENSE") {
    return bootstrap.expenseCategories;
  }
  return [];
}

export function createSplitRow(categoryId = "") {
  return {
    categoryId,
    amount: "",
    note: "",
  };
}

export function createTransactionForm(bootstrap, period, type = "EXPENSE") {
  return {
    type,
    amount: "",
    transactionDate: today(),
    sourceAccountId: firstOptionId(bootstrap.accounts),
    targetAccountId: "",
    categoryId: type === "TRANSFER" ? "" : firstOptionId(categoriesByType(bootstrap, type)),
    merchant: "",
    note: period ? `${period} 账单记录` : "",
    tagsInput: "",
    splitMode: false,
    splits: [],
  };
}

export function createBudgetForm(bootstrap, period) {
  return {
    period,
    categoryId: firstOptionId(bootstrap.expenseCategories),
    amount: "",
  };
}

export function createRecurringForm(bootstrap, type = "EXPENSE") {
  return {
    title: "",
    type,
    frequency: "MONTHLY",
    startDate: today(),
    nextRunDate: nextMonthDay(1),
    endDate: "",
    sourceAccountId: firstOptionId(bootstrap.accounts),
    targetAccountId: "",
    categoryId: type === "TRANSFER" ? "" : firstOptionId(categoriesByType(bootstrap, type)),
    amount: "",
    merchant: "",
    note: "",
    tagsInput: "",
    active: true,
  };
}

export function buildTransactionPayload(form) {
  return {
    type: form.type,
    amount: Number(form.amount),
    transactionDate: form.transactionDate,
    sourceAccountId: Number(form.sourceAccountId),
    targetAccountId:
      form.type === "TRANSFER" && form.targetAccountId
        ? Number(form.targetAccountId)
        : null,
    categoryId:
      form.type !== "TRANSFER" && !form.splitMode && form.categoryId
        ? Number(form.categoryId)
        : null,
    merchant: trimToNull(form.merchant),
    note: trimToNull(form.note),
    labels: trimToNull(form.tagsInput),
    splits: form.splitMode
      ? form.splits
          .filter((item) => item.categoryId && item.amount)
          .map((item) => ({
            categoryId: Number(item.categoryId),
            amount: Number(item.amount),
            note: trimToNull(item.note),
          }))
      : [],
  };
}

export function buildBudgetPayload(form) {
  return {
    period: form.period,
    categoryId: Number(form.categoryId),
    amount: Number(form.amount),
  };
}

export function buildRecurringPayload(form) {
  return {
    title: form.title,
    type: form.type,
    frequency: form.frequency,
    startDate: form.startDate,
    nextRunDate: form.nextRunDate,
    endDate: form.endDate || null,
    sourceAccountId: Number(form.sourceAccountId),
    targetAccountId:
      form.type === "TRANSFER" && form.targetAccountId
        ? Number(form.targetAccountId)
        : null,
    categoryId:
      form.type !== "TRANSFER" && form.categoryId ? Number(form.categoryId) : null,
    amount: Number(form.amount),
    merchant: trimToNull(form.merchant),
    note: trimToNull(form.note),
    labels: trimToNull(form.tagsInput),
    active: Boolean(form.active),
  };
}

export function transactionTitle(item) {
  return (
    item.merchant ||
    item.splitSummary ||
    item.categoryName ||
    item.targetAccountName ||
    "未命名流水"
  );
}
