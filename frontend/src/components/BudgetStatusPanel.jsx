import { formatCurrency, formatPercent } from "../helpers";

export default function BudgetStatusPanel({ budgets }) {
  return (
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
  );
}
