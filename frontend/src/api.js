import axios from "axios";

export const SESSION_KEY = "ledger-pro-session";

const api = axios.create({
  baseURL: "/api",
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const session = loadSession();
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`;
  }
  return config;
});

export function loadSession() {
  try {
    const raw = window.localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
}

export function saveSession(session) {
  window.localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
  window.localStorage.removeItem(SESSION_KEY);
}

export async function login(payload) {
  const response = await api.post("/auth/login", payload);
  return response.data;
}

export async function logout() {
  await api.post("/auth/logout");
}

export async function fetchCurrentUser() {
  const response = await api.get("/auth/me");
  return response.data;
}

export async function fetchBootstrap() {
  const response = await api.get("/lookups/bootstrap");
  return response.data;
}

export async function fetchDashboard(period) {
  const response = await api.get("/dashboard", { params: { period } });
  return response.data;
}

export async function fetchTransactions(params) {
  const response = await api.get("/transactions", { params });
  return response.data;
}

export async function createTransaction(payload) {
  const response = await api.post("/transactions", payload);
  return response.data;
}

export async function updateTransaction(id, payload) {
  const response = await api.put(`/transactions/${id}`, payload);
  return response.data;
}

export async function removeTransaction(id) {
  await api.delete(`/transactions/${id}`);
}

export async function fetchBudgets(period) {
  const response = await api.get("/budgets", { params: { period } });
  return response.data;
}

export async function saveBudget(payload) {
  const response = await api.post("/budgets", payload);
  return response.data;
}

export async function fetchRecurringRules() {
  const response = await api.get("/recurring-rules");
  return response.data;
}

export async function createRecurringRule(payload) {
  const response = await api.post("/recurring-rules", payload);
  return response.data;
}

export async function updateRecurringRule(id, payload) {
  const response = await api.put(`/recurring-rules/${id}`, payload);
  return response.data;
}

export async function removeRecurringRule(id) {
  await api.delete(`/recurring-rules/${id}`);
}

export async function processRecurringRules() {
  const response = await api.post("/recurring-rules/process");
  return response.data;
}

export async function exportTransactions(params) {
  return downloadFile("/exports/transactions.csv", params);
}

export async function exportDashboard(period) {
  return downloadFile("/exports/dashboard.csv", { period });
}

async function downloadFile(url, params) {
  const response = await api.get(url, {
    params,
    responseType: "blob",
  });

  const disposition = response.headers["content-disposition"] || "";
  const match = disposition.match(/filename="?([^"]+)"?/i);
  const filename = match?.[1] || "export.csv";
  const blobUrl = window.URL.createObjectURL(response.data);
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
}

export function getErrorMessage(error) {
  return error?.response?.data?.message || error?.message || "请求失败";
}

export function isUnauthorized(error) {
  return error?.response?.status === 401;
}
