export default function BudgetFormPanel({
  bootstrap,
  budgetForm,
  onFieldChange,
  onSubmit,
}) {
  return (
    <article id="budgets" className="panel">
      <div className="panel-head">
        <div>
          <p className="eyebrow">Budget Form</p>
          <h3>预算策略配置</h3>
          <p className="panel-subcopy">按月份和支出分类设置本期预算阈值。</p>
        </div>
      </div>
      <form className="form-grid" onSubmit={onSubmit}>
        <label>
          月份
          <input
            type="month"
            value={budgetForm.period}
            onChange={(event) => onFieldChange("period", event.target.value)}
            required
          />
        </label>

        <label>
          分类
          <select
            value={budgetForm.categoryId}
            onChange={(event) => onFieldChange("categoryId", event.target.value)}
            required
          >
            {bootstrap.expenseCategories.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name}
              </option>
            ))}
          </select>
        </label>

        <label className="span-2">
          预算金额
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={budgetForm.amount}
            onChange={(event) => onFieldChange("amount", event.target.value)}
            required
          />
        </label>

        <button className="primary-button span-2" type="submit">
          保存预算
        </button>
      </form>
    </article>
  );
}
