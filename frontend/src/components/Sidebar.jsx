import {
  CalendarRange,
  LayoutDashboard,
  LogOut,
  Sparkles,
  UserRound,
} from "lucide-react";
import { formatCurrency } from "../helpers";

export default function Sidebar({
  user,
  period,
  transactionsCount,
  netWorth,
  budgetAlertCount,
  recurringCount,
  firstInsight,
  onPeriodChange,
  onLogout,
}) {
  return (
    <aside className="sidebar">
      <div className="brand-card">
        <div className="brand-mark">
          <LayoutDashboard size={22} />
        </div>
        <div>
          <p className="eyebrow">Ledger Pro</p>
          <h1>智能记账工作台</h1>
        </div>
      </div>

      <div className="sidebar-card profile-card">
        <div className="profile-row">
          <div className="profile-badge">
            <UserRound size={18} />
          </div>
          <div>
            <strong>{user.displayName}</strong>
            <p>@{user.username}</p>
          </div>
        </div>
        <button type="button" className="ghost-button" onClick={onLogout}>
          <LogOut size={16} />
          退出登录
        </button>
      </div>

      <nav className="sidebar-card nav-stack">
        <a href="#overview">概览</a>
        <a href="#entry">录入流水</a>
        <a href="#transactions">流水中心</a>
        <a href="#budgets">预算管理</a>
        <a href="#recurring">定时记账</a>
        <a href="#accounts">账户视图</a>
      </nav>

      <div className="sidebar-card">
        <label className="field-label">
          <CalendarRange size={16} />
          统计月份
        </label>
        <input type="month" value={period} onChange={onPeriodChange} />

        <div className="mini-stat">
          <span>净资产</span>
          <strong>{formatCurrency(netWorth)}</strong>
        </div>
        <div className="mini-stat">
          <span>预算预警</span>
          <strong>{budgetAlertCount} 项</strong>
        </div>
        <div className="mini-stat">
          <span>流水数量</span>
          <strong>{transactionsCount} 笔</strong>
        </div>
        <div className="mini-stat">
          <span>定时规则</span>
          <strong>{recurringCount} 条</strong>
        </div>
      </div>

      <div className="sidebar-card signal-card">
        <div className="signal-title">
          <Sparkles size={16} />
          核心提示
        </div>
        <p>{firstInsight || "这里会展示当月支出重点、预算提醒和消费节奏。"}</p>
      </div>
    </aside>
  );
}
