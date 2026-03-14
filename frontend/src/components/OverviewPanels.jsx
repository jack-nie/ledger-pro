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
  onExportDashboard,
  onExportTransactions,
}) {
  return (
    <>
      <section id="overview" className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Resource Overview</p>
          <h2>当前账期运行状态</h2>
          <p>
            这里聚合展示本月收入、支出、账户余额和消费结构，布局参考云控制台资源概览页。
          </p>
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
            {loading ? "正在同步数据" : "资源状态正常"}
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
              <h3>近 6 个月收支趋势</h3>
              <p className="panel-subcopy">观察月度收入和支出的波动曲线。</p>
            </div>
          </div>
          <div className="chart-wrap">
            <ResponsiveContainer width="100%" height={280}>
              <AreaChart data={dashboard?.trend || []}>
                <defs>
                  <linearGradient id="incomeFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#0064ff" stopOpacity={0.4} />
                    <stop offset="100%" stopColor="#0064ff" stopOpacity={0.02} />
                  </linearGradient>
                  <linearGradient id="expenseFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#36cfc9" stopOpacity={0.3} />
                    <stop offset="100%" stopColor="#36cfc9" stopOpacity={0.02} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="month" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} tickFormatter={formatCurrency} />
                <Tooltip formatter={(value) => formatCurrency(value)} />
                <Area
                  type="monotone"
                  dataKey="income"
                  stroke="#0064ff"
                  fill="url(#incomeFill)"
                  strokeWidth={2}
                />
                <Area
                  type="monotone"
                  dataKey="expense"
                  stroke="#36cfc9"
                  fill="url(#expenseFill)"
                  strokeWidth={2}
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
              <p className="panel-subcopy">查看当前账期的消费结构分布。</p>
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
                    <Cell key={item.categoryId} fill={item.categoryColor || "#0064ff"} />
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
              <p className="eyebrow">Insights</p>
              <h3>运行提醒</h3>
              <p className="panel-subcopy">输出预算、消费节奏和资金变化提示。</p>
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
