import {
  ChartColumn,
  CreditCard,
  LayoutDashboard,
  LogOut,
  Repeat,
  SquarePen,
  UserRound,
} from "lucide-react";
import { formatCurrency } from "../helpers";

const navItems = [
  { id: "overview", label: "总览", icon: LayoutDashboard },
  { id: "entry", label: "录入流水", icon: SquarePen },
  { id: "transactions", label: "流水列表", icon: ChartColumn },
  { id: "budgets", label: "预算管理", icon: CreditCard },
  { id: "recurring", label: "周期规则", icon: Repeat },
  { id: "accounts", label: "账户视图", icon: ChartColumn },
];

export default function Sidebar({
  user,
  activeTab,
  transactionsCount,
  netWorth,
  budgetAlertCount,
  recurringCount,
  firstInsight,
  onTabChange,
  onLogout,
}) {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-mark">LP</div>
        <div>
          <p className="eyebrow">Ledger Pro</p>
          <h1>财务控制台</h1>
        </div>
      </div>

      <div className="sidebar-profile">
        <div className="profile-row">
          <div className="profile-badge">
            <UserRound size={18} />
          </div>
          <div>
            <strong>{user.displayName}</strong>
            <p>@{user.username}</p>
          </div>
        </div>
        <span className="sidebar-tag">Console Mode</span>
      </div>

      <div className="sidebar-block">
        <div className="sidebar-block-title">主导航</div>
        <nav className="nav-stack" aria-label="Workspace tabs">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                type="button"
                className={activeTab === item.id ? "nav-button active" : "nav-button"}
                onClick={() => onTabChange(item.id)}
              >
                <span className="nav-icon">
                  <Icon size={16} />
                </span>
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      <div className="sidebar-block">
        <div className="sidebar-block-title">资源概览</div>
        <div className="sidebar-stat-grid">
          <div className="sidebar-stat-card">
            <span>净资产</span>
            <strong>{formatCurrency(netWorth)}</strong>
          </div>
          <div className="sidebar-stat-card">
            <span>预算预警</span>
            <strong>{budgetAlertCount}</strong>
          </div>
          <div className="sidebar-stat-card">
            <span>流水数量</span>
            <strong>{transactionsCount}</strong>
          </div>
          <div className="sidebar-stat-card">
            <span>周期规则</span>
            <strong>{recurringCount}</strong>
          </div>
        </div>
      </div>

      <div className="sidebar-block sidebar-insight">
        <div className="sidebar-block-title">运行提示</div>
        <p>{firstInsight || "当前账本已切换到控制台布局，可按左侧模块逐项管理。"} </p>
      </div>

      <button type="button" className="logout-button" onClick={onLogout}>
        <LogOut size={16} />
        退出登录
      </button>
    </aside>
  );
}
