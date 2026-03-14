import { formatCurrency, formatPercent } from "../helpers";

export default function BudgetStatusPanel({ budgets }) {
  return (
    <article className="panel">
      <div className="panel-head">
        <div>
          <p className="eyebrow">Budget Board</p>
          <h3>预算执行看板</h3>
          <p className="panel-subcopy">以控制台资源列表方式查看本月预算使用率和风险状态。</p>
        </div>
      </div>

      <div className="budget-list">
        {budgets.length ? (
          budgets.map((item) => (
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
                        ? "#d93026"
                        : item.status === "RISK"
                          ? "#ff8a00"
                          : item.categoryColor || "#0064ff",
                  }}
                />
              </div>
              <p>
                已用 {formatCurrency(item.spent)} / 预算 {formatCurrency(item.amount)}
              </p>
            </div>
          ))
        ) : (
          <div className="empty-state">当前月份还没有预算配置。</div>
        )}
      </div>
    </article>
  );
}
