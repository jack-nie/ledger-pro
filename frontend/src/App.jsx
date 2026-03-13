import { startTransition, useDeferredValue, useEffect, useState } from "react";
import {
  clearSession,
  createRecurringRule,
  createTransaction,
  exportDashboard,
  exportTransactions,
  fetchBootstrap,
  fetchBudgets,
  fetchCurrentUser,
  fetchDashboard,
  fetchRecurringRules,
  fetchTransactions,
  getErrorMessage,
  isUnauthorized,
  loadSession,
  login,
  logout,
  processRecurringRules,
  removeRecurringRule,
  removeTransaction,
  saveBudget,
  saveSession,
  updateRecurringRule,
  updateTransaction,
} from "./api";
import {
  buildBudgetPayload,
  buildRecurringPayload,
  buildTransactionPayload,
  categoriesByType,
  createBudgetForm,
  createRecurringForm,
  createSplitRow,
  createTransactionForm,
  emptyBootstrap,
  firstOptionId,
} from "./helpers";
import BudgetFormPanel from "./components/BudgetFormPanel";
import LoginPage from "./components/LoginPage";
import OverviewPanels from "./components/OverviewPanels";
import RecurringSection from "./components/RecurringSection";
import Sidebar from "./components/Sidebar";
import TransactionSection from "./components/TransactionSection";

export default function App() {
  const initialSession = loadSession();
  const [session, setSession] = useState(initialSession);
  const [user, setUser] = useState(
    initialSession
      ? {
          userId: initialSession.userId,
          username: initialSession.username,
          displayName: initialSession.displayName,
        }
      : null
  );
  const [authChecking, setAuthChecking] = useState(Boolean(initialSession?.token));
  const [authSubmitting, setAuthSubmitting] = useState(false);
  const [loginForm, setLoginForm] = useState({
    username: initialSession?.username || "demo",
    password: "123456",
  });

  const [bootstrap, setBootstrap] = useState(emptyBootstrap);
  const [period, setPeriod] = useState("");
  const [dashboard, setDashboard] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [recurringRules, setRecurringRules] = useState([]);
  const [workspaceLoading, setWorkspaceLoading] = useState(false);
  const [reloadToken, setReloadToken] = useState(0);
  const [error, setError] = useState("");
  const [status, setStatus] = useState("");

  const [editingTransactionId, setEditingTransactionId] = useState(null);
  const [transactionForm, setTransactionForm] = useState(
    createTransactionForm(emptyBootstrap, "")
  );
  const [budgetForm, setBudgetForm] = useState(createBudgetForm(emptyBootstrap, ""));
  const [editingRuleId, setEditingRuleId] = useState(null);
  const [recurringForm, setRecurringForm] = useState(
    createRecurringForm(emptyBootstrap)
  );
  const [filters, setFilters] = useState({
    type: "",
    categoryId: "",
    keyword: "",
  });

  const deferredKeyword = useDeferredValue(filters.keyword);
  const allCategories = [
    ...bootstrap.expenseCategories,
    ...bootstrap.incomeCategories,
  ];
  const splitTotal = transactionForm.splits.reduce(
    (sum, item) => sum + Number(item.amount || 0),
    0
  );
  const popularTags = Array.from(
    new Set(transactions.flatMap((item) => item.tags || []))
  ).slice(0, 8);
  const netWorth =
    dashboard?.accounts?.reduce(
      (sum, item) => sum + Number(item.balance || 0),
      0
    ) || 0;
  const budgetAlertCount = budgets.filter(
    (item) => item.status === "RISK" || item.status === "OVER"
  ).length;

  useEffect(() => {
    let active = true;

    async function restoreSession() {
      if (!initialSession?.token) {
        setAuthChecking(false);
        return;
      }

      try {
        const [profile, bootstrapData] = await Promise.all([
          fetchCurrentUser(),
          fetchBootstrap(),
        ]);

        if (!active) {
          return;
        }

        startTransition(() => {
          setUser(profile);
          applyBootstrapState(bootstrapData);
        });
      } catch (requestError) {
        if (active) {
          resetSession();
          setError("登录状态已失效，请重新登录。");
        }
      } finally {
        if (active) {
          setAuthChecking(false);
        }
      }
    }

    restoreSession();
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!user || !period) {
      return undefined;
    }

    let active = true;

    async function loadWorkspace() {
      setWorkspaceLoading(true);
      setError("");
      try {
        const [dashboardData, transactionData, budgetData, recurringData] =
          await Promise.all([
            fetchDashboard(period),
            fetchTransactions({
              period,
              type: filters.type || undefined,
              categoryId: filters.categoryId || undefined,
              keyword: deferredKeyword || undefined,
            }),
            fetchBudgets(period),
            fetchRecurringRules(),
          ]);

        if (!active) {
          return;
        }

        startTransition(() => {
          setDashboard(dashboardData);
          setTransactions(transactionData);
          setBudgets(budgetData);
          setRecurringRules(recurringData);
        });
      } catch (requestError) {
        if (active) {
          handleRequestError(requestError);
        }
      } finally {
        if (active) {
          setWorkspaceLoading(false);
        }
      }
    }

    loadWorkspace();
    return () => {
      active = false;
    };
  }, [user, period, filters.type, filters.categoryId, deferredKeyword, reloadToken]);

  function applyBootstrapState(data) {
    const nextPeriod = data.currentPeriod;
    setBootstrap(data);
    setPeriod(nextPeriod);
    setTransactionForm(createTransactionForm(data, nextPeriod));
    setBudgetForm(createBudgetForm(data, nextPeriod));
    setRecurringForm(createRecurringForm(data));
  }

  function resetSession() {
    clearSession();
    setSession(null);
    setUser(null);
    setBootstrap(emptyBootstrap);
    setPeriod("");
    setDashboard(null);
    setTransactions([]);
    setBudgets([]);
    setRecurringRules([]);
    setEditingTransactionId(null);
    setEditingRuleId(null);
    setTransactionForm(createTransactionForm(emptyBootstrap, ""));
    setBudgetForm(createBudgetForm(emptyBootstrap, ""));
    setRecurringForm(createRecurringForm(emptyBootstrap));
  }

  function handleRequestError(requestError) {
    if (isUnauthorized(requestError)) {
      resetSession();
      setError("登录状态已失效，请重新登录。");
      return;
    }
    setError(getErrorMessage(requestError));
  }

  async function handleLoginSubmit(event) {
    event.preventDefault();
    setAuthSubmitting(true);
    setError("");

    try {
      const loginResponse = await login(loginForm);
      saveSession(loginResponse);
      setSession(loginResponse);
      setUser({
        userId: loginResponse.userId,
        username: loginResponse.username,
        displayName: loginResponse.displayName,
      });

      const bootstrapData = await fetchBootstrap();
      startTransition(() => {
        applyBootstrapState(bootstrapData);
      });
      setStatus("已进入账本工作台");
    } catch (requestError) {
      handleRequestError(requestError);
    } finally {
      setAuthSubmitting(false);
      setAuthChecking(false);
    }
  }

  async function handleLogout() {
    try {
      await logout();
    } catch (requestError) {
      // ignore logout failure
    } finally {
      resetSession();
      setStatus("已退出登录");
    }
  }

  async function handleTransactionSubmit(event) {
    event.preventDefault();
    setError("");
    setStatus("");

    try {
      const payload = buildTransactionPayload(transactionForm);
      if (editingTransactionId) {
        await updateTransaction(editingTransactionId, payload);
        setStatus("流水已更新");
      } else {
        await createTransaction(payload);
        setStatus("流水已创建");
      }
      setEditingTransactionId(null);
      setTransactionForm(createTransactionForm(bootstrap, period));
      setReloadToken((value) => value + 1);
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleBudgetSubmit(event) {
    event.preventDefault();
    setError("");
    setStatus("");

    try {
      await saveBudget(buildBudgetPayload(budgetForm));
      setStatus("预算已保存");
      setBudgetForm(createBudgetForm(bootstrap, period));
      setReloadToken((value) => value + 1);
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleRecurringSubmit(event) {
    event.preventDefault();
    setError("");
    setStatus("");

    try {
      const payload = buildRecurringPayload(recurringForm);
      if (editingRuleId) {
        await updateRecurringRule(editingRuleId, payload);
        setStatus("定时规则已更新");
      } else {
        await createRecurringRule(payload);
        setStatus("定时规则已创建");
      }
      setEditingRuleId(null);
      setRecurringForm(createRecurringForm(bootstrap));
      setReloadToken((value) => value + 1);
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleDeleteTransaction(id) {
    try {
      await removeTransaction(id);
      if (editingTransactionId === id) {
        setEditingTransactionId(null);
        setTransactionForm(createTransactionForm(bootstrap, period));
      }
      setStatus("流水已删除");
      setReloadToken((value) => value + 1);
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleDeleteRecurringRule(id) {
    try {
      await removeRecurringRule(id);
      if (editingRuleId === id) {
        setEditingRuleId(null);
        setRecurringForm(createRecurringForm(bootstrap));
      }
      setStatus("定时规则已删除");
      setReloadToken((value) => value + 1);
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleProcessRecurringRules() {
    try {
      const result = await processRecurringRules();
      setStatus(`已补跑到期规则，生成 ${result.createdCount} 笔流水`);
      setReloadToken((value) => value + 1);
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleExportTransactions() {
    try {
      await exportTransactions({
        period,
        type: filters.type || undefined,
        categoryId: filters.categoryId || undefined,
        keyword: deferredKeyword || undefined,
      });
      setStatus("流水 CSV 已导出");
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  async function handleExportDashboard() {
    try {
      await exportDashboard(period);
      setStatus("统计报表 CSV 已导出");
    } catch (requestError) {
      handleRequestError(requestError);
    }
  }

  function handleTransactionField(field, value) {
    if (field !== "type") {
      setTransactionForm((current) => ({ ...current, [field]: value }));
      return;
    }

    const nextCategory = firstOptionId(categoriesByType(bootstrap, value));
    setTransactionForm((current) => ({
      ...current,
      type: value,
      categoryId: value === "TRANSFER" ? "" : nextCategory,
      targetAccountId: value === "TRANSFER" ? current.targetAccountId : "",
      splitMode: value === "TRANSFER" ? false : current.splitMode,
      splits: value === "TRANSFER" ? [] : current.splits,
    }));
  }

  function handleRecurringField(field, value) {
    if (field !== "type") {
      setRecurringForm((current) => ({ ...current, [field]: value }));
      return;
    }

    const nextCategory = firstOptionId(categoriesByType(bootstrap, value));
    setRecurringForm((current) => ({
      ...current,
      type: value,
      categoryId: value === "TRANSFER" ? "" : nextCategory,
      targetAccountId: value === "TRANSFER" ? current.targetAccountId : "",
    }));
  }

  function toggleSplitMode() {
    setTransactionForm((current) => {
      if (current.type === "TRANSFER") {
        return current;
      }
      if (current.splitMode) {
        return {
          ...current,
          splitMode: false,
          splits: [],
          categoryId: firstOptionId(categoriesByType(bootstrap, current.type)),
        };
      }
      return {
        ...current,
        splitMode: true,
        categoryId: "",
        splits: [
          createSplitRow(firstOptionId(categoriesByType(bootstrap, current.type))),
        ],
      };
    });
  }

  function updateSplit(index, field, value) {
    setTransactionForm((current) => ({
      ...current,
      splits: current.splits.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      ),
    }));
  }

  function addSplit() {
    setTransactionForm((current) => ({
      ...current,
      splits: [
        ...current.splits,
        createSplitRow(firstOptionId(categoriesByType(bootstrap, current.type))),
      ],
    }));
  }

  function removeSplit(index) {
    setTransactionForm((current) => ({
      ...current,
      splits: current.splits.filter((_, itemIndex) => itemIndex !== index),
    }));
  }

  function editTransaction(item) {
    setEditingTransactionId(item.id);
    setTransactionForm({
      type: item.type,
      amount: String(item.amount),
      transactionDate: item.transactionDate,
      sourceAccountId: String(item.sourceAccountId),
      targetAccountId: item.targetAccountId ? String(item.targetAccountId) : "",
      categoryId: item.categoryId ? String(item.categoryId) : "",
      merchant: item.merchant || "",
      note: item.note || "",
      tagsInput: item.labels || "",
      splitMode: Boolean(item.hasSplit),
      splits: (item.splits || []).map((split) => ({
        categoryId: String(split.categoryId),
        amount: String(split.amount),
        note: split.note || "",
      })),
    });
    window.location.hash = "#entry";
  }

  function editRecurringRule(item) {
    setEditingRuleId(item.id);
    setRecurringForm({
      title: item.title,
      type: item.type,
      frequency: item.frequency,
      startDate: item.startDate,
      nextRunDate: item.nextRunDate,
      endDate: item.endDate || "",
      sourceAccountId: String(item.sourceAccountId),
      targetAccountId: item.targetAccountId ? String(item.targetAccountId) : "",
      categoryId: item.categoryId ? String(item.categoryId) : "",
      amount: String(item.amount),
      merchant: item.merchant || "",
      note: item.note || "",
      tagsInput: item.labels || "",
      active: item.active,
    });
    window.location.hash = "#recurring";
  }

  if (authChecking) {
    return <div className="splash-screen">正在恢复账本会话...</div>;
  }

  if (!session?.token || !user) {
    return (
      <LoginPage
        error={error}
        loginForm={loginForm}
        authSubmitting={authSubmitting}
        onFieldChange={(field, value) =>
          setLoginForm((current) => ({ ...current, [field]: value }))
        }
        onSubmit={handleLoginSubmit}
      />
    );
  }

  return (
    <div className="app-shell">
      <Sidebar
        user={user}
        period={period}
        transactionsCount={transactions.length}
        netWorth={netWorth}
        budgetAlertCount={budgetAlertCount}
        recurringCount={recurringRules.length}
        firstInsight={dashboard?.insights?.[0]?.description}
        onPeriodChange={(event) => setPeriod(event.target.value)}
        onLogout={handleLogout}
      />

      <main className="content">
        {error ? <div className="message error">{error}</div> : null}
        {status ? <div className="message success">{status}</div> : null}

        <OverviewPanels
          loading={workspaceLoading}
          dashboard={dashboard}
          budgets={budgets}
          onExportDashboard={handleExportDashboard}
          onExportTransactions={handleExportTransactions}
        />

        <section className="grid-layout">
          <TransactionSection
            bootstrap={bootstrap}
            period={period}
            allCategories={allCategories}
            filters={filters}
            popularTags={popularTags}
            transactionForm={transactionForm}
            transactions={transactions}
            editingTransactionId={editingTransactionId}
            splitTotal={splitTotal}
            onFilterChange={(field, value) =>
              setFilters((current) => ({ ...current, [field]: value }))
            }
            onExport={handleExportTransactions}
            onSubmit={handleTransactionSubmit}
            onEdit={editTransaction}
            onDelete={handleDeleteTransaction}
            onResetEdit={() => {
              setEditingTransactionId(null);
              setTransactionForm(createTransactionForm(bootstrap, period));
            }}
            onFieldChange={handleTransactionField}
            onToggleSplitMode={toggleSplitMode}
            onSplitChange={updateSplit}
            onAddSplit={addSplit}
            onRemoveSplit={removeSplit}
          />

          <BudgetFormPanel
            bootstrap={bootstrap}
            budgetForm={budgetForm}
            onFieldChange={(field, value) =>
              setBudgetForm((current) => ({ ...current, [field]: value }))
            }
            onSubmit={handleBudgetSubmit}
          />

          <RecurringSection
            bootstrap={bootstrap}
            recurringForm={recurringForm}
            recurringRules={recurringRules}
            editingRuleId={editingRuleId}
            onFieldChange={handleRecurringField}
            onSubmit={handleRecurringSubmit}
            onProcess={handleProcessRecurringRules}
            onEdit={editRecurringRule}
            onDelete={handleDeleteRecurringRule}
            onResetEdit={() => {
              setEditingRuleId(null);
              setRecurringForm(createRecurringForm(bootstrap));
            }}
          />
        </section>
      </main>
    </div>
  );
}
