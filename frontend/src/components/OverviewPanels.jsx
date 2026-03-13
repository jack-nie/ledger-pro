import {
  AlertTriangle,
  ArrowDownToLine,
  CircleDollarSign,
  Clock3,
  Download,
  Landmark,
  PiggyBank,
  Target,
  TrendingUp,
  Wallet,
} from "lucide-react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatCurrency, formatPercent } from "../helpers";

export default function OverviewPanels({
  loading,
  dashboard,
  budgets,
  onExportDashboard,
  onExportTransactions,
}) {
  return (
    <>
      <section id="overview" className="hero">
        <div>
          <p className="eyebrow">Spring Boot + React + MySQL + Redis</p>
          <h2>把流水、预算、分账、导出和定时记账统一在一个工作台里</h2>
        </div>
        <div className="hero-actions">
          <button type="button" className="ghost-button" onClick={onExportDashboard}>
            <Download size={16} />
            导出统计
          </button>
          <button type="button" className="ghost-button" onClick={onExportTransactions}>
            <ArrowDownToLine size={16} />
            导出流水
          </button>
          <div className="hero-chip">
            <PiggyBank size={18} />
            {loading ? "同步数据中" : "已接入鉴权与缓存"}
          </div>
        </div>
      </section>

      <section className="metric-grid">
        <article className="metric-card">
          <div className="metric-icon income">
            <TrendingUp size={18} />
          </div>
          <span>本月收入</span>
          <strong>{formatCurrency(dashboard?.summary?.income)}</strong>
        </article>
        <article className="metric-card">
          <div className="metric-icon expense">
            <Wallet size={18} />
          </div>
          <span>本月支出</span>
          <strong>{formatCurrency(dashboard?.summary?.expense)}</strong>
        </article>
        <article className="metric-card">
          <div className="metric-icon balance">
            <Landmark size={18} />
          </div>
          <span>本月结余</span>
          <strong>{formatCurrency(dashboard?.summary?.balance)}</strong>
        </article>
        <article className="metric-card">
          <div className="metric-icon savings">
            <Target size={18} />
          </div>
          <span>储蓄率</span>
          <strong>{formatPercent(dashboard?.summary?.savingsRate)}</strong>
        </article>
      </section>

      <section className="grid-layout">
        <article className="panel panel-wide">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Cash Flow</p>
              <h3>近 6 个月收入与支出</h3>
            </div>
          </div>
          <div className="chart-wrap">
            <ResponsiveContainer width="100%" height={280}>
              <AreaChart data={dashboard?.trend || []}>
                <defs>
                  <linearGradient id="incomeFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#0f766e" stopOpacity={0.58} />
                    <stop offset="100%" stopColor="#0f766e" stopOpacity={0.03} />
                  </linearGradient>
                  <linearGradient id="expenseFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#dc2626" stopOpacity={0.45} />
                    <stop offset="100%" stopColor="#dc2626" stopOpacity={0.03} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="month" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} tickFormatter={formatCurrency} />
                <Tooltip formatter={(value) => formatCurrency(value)} />
                <Area
                  type="monotone"
                  dataKey="income"
                  stroke="#0f766e"
                  fill="url(#incomeFill)"
                  strokeWidth={2.2}
                />
                <Area
                  type="monotone"
                  dataKey="expense"
                  stroke="#dc2626"
                  fill="url(#expenseFill)"
                  strokeWidth={2.2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Spending Mix</p>
              <h3>支出分类占比</h3>
            </div>
          </div>
          <div className="chart-wrap">
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={dashboard?.categoryBreakdown || []}
                  dataKey="amount"
                  nameKey="categoryName"
                  innerRadius={64}
                  outerRadius={96}
                  paddingAngle={2}
                >
                  {(dashboard?.categoryBreakdown || []).map((item) => (
                    <Cell key={item.categoryId} fill={item.categoryColor || "#0f766e"} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => formatCurrency(value)} />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="legend-stack">
            {(dashboard?.categoryBreakdown || []).slice(0, 5).map((item) => (
              <div className="legend-row" key={item.categoryId}>
                <span className="legend-dot" style={{ background: item.categoryColor }} />
                <span>{item.categoryName}</span>
                <strong>{formatPercent(item.share)}</strong>
              </div>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Budget Board</p>
              <h3>预算执行</h3>
            </div>
          </div>
          <div className="budget-list">
            {budgets.map((item) => (
              <div className="budget-item" key={`${item.period}-${item.categoryId}`}>
                <div className="budget-topline">
                  <span>{item.categoryName}</span>
                  <strong>{formatPercent(item.usageRate)}</strong>
                </div>
                <div className="progress-bar">
                  <span
                    style={{
                      width: `${Math.min(Number(item.usageRate || 0), 100)}%`,
                      background:
                        item.status === "OVER"
                          ? "#dc2626"
                          : item.status === "RISK"
                            ? "#d97706"
                            : item.categoryColor || "#0f766e",
                    }}
                  />
                </div>
                <p>
                  已用 {formatCurrency(item.spent)} / 预算 {formatCurrency(item.amount)}
                </p>
              </div>
            ))}
          </div>
        </article>

        <article id="accounts" className="panel panel-wide">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Accounts</p>
              <h3>账户分布</h3>
            </div>
          </div>
          <div className="chart-wrap small">
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={dashboard?.accounts || []}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} tickFormatter={formatCurrency} />
                <Tooltip formatter={(value) => formatCurrency(value)} />
                <Bar dataKey="balance" radius={[12, 12, 0, 0]}>
                  {(dashboard?.accounts || []).map((item) => (
                    <Cell key={item.id} fill={item.colorHex || "#0f766e"} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="account-grid">
            {(dashboard?.accounts || []).map((item) => (
              <article className="account-card" key={item.id}>
                <span className="account-color" style={{ background: item.colorHex }} />
                <div>
                  <h4>{item.name}</h4>
                  <p>{item.type}</p>
                </div>
                <strong>{formatCurrency(item.balance)}</strong>
              </article>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Insights</p>
              <h3>本月提醒</h3>
            </div>
          </div>
          <div className="insight-list">
            {(dashboard?.insights || []).map((item) => (
              <div className="insight-item" key={item.title}>
                <div className="insight-icon">
                  {item.title.includes("预算") ? (
                    <AlertTriangle size={16} />
                  ) : item.title.includes("节奏") ? (
                    <Clock3 size={16} />
                  ) : (
                    <CircleDollarSign size={16} />
                  )}
                </div>
                <div>
                  <h4>{item.title}</h4>
                  <p>{item.description}</p>
                </div>
              </div>
            ))}
          </div>
        </article>
      </section>
    </>
  );
}
