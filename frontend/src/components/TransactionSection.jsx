import {
  ArrowRightLeft,
  Download,
  Pencil,
  Plus,
  Search,
  Trash2,
} from "lucide-react";
import {
  categoriesByType,
  formatCurrency,
  transactionTitle,
  typeLabels,
} from "../helpers";

export default function TransactionSection({
  view = "all",
  bootstrap,
  allCategories,
  filters,
  popularTags,
  transactionForm,
  transactions,
  editingTransactionId,
  splitTotal,
  onFilterChange,
  onExport,
  onSubmit,
  onEdit,
  onDelete,
  onResetEdit,
  onFieldChange,
  onToggleSplitMode,
  onSplitChange,
  onAddSplit,
  onRemoveSplit,
}) {
  const currentCategories = categoriesByType(bootstrap, transactionForm.type);
  const showEntry = view === "all" || view === "entry";
  const showTransactions = view === "all" || view === "transactions";

  return (
    <>
      {showEntry ? (
        <article id="entry" className="panel">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Quick Entry</p>
              <h3>{editingTransactionId ? "编辑流水" : "新增流水"}</h3>
            </div>
            <div className="panel-actions">
              <button
                type="button"
                className="ghost-button"
                onClick={onToggleSplitMode}
              >
                <ArrowRightLeft size={16} />
                {transactionForm.splitMode ? "关闭分账" : "启用分账"}
              </button>
              {editingTransactionId ? (
                <button type="button" className="ghost-button" onClick={onResetEdit}>
                  取消编辑
                </button>
              ) : null}
            </div>
          </div>

          <form className="form-grid" onSubmit={onSubmit}>
            <div className="chip-group span-2">
              {["EXPENSE", "INCOME", "TRANSFER"].map((type) => (
                <button
                  type="button"
                  key={type}
                  className={
                    transactionForm.type === type ? "chip-button active" : "chip-button"
                  }
                  onClick={() => onFieldChange("type", type)}
                >
                  {typeLabels[type]}
                </button>
              ))}
            </div>

            <label>
              金额
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={transactionForm.amount}
                onChange={(event) => onFieldChange("amount", event.target.value)}
                required
              />
            </label>

            <label>
              日期
              <input
                type="date"
                value={transactionForm.transactionDate}
                onChange={(event) => onFieldChange("transactionDate", event.target.value)}
                required
              />
            </label>

            <label>
              付款账户
              <select
                value={transactionForm.sourceAccountId}
                onChange={(event) => onFieldChange("sourceAccountId", event.target.value)}
                required
              >
                {bootstrap.accounts.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </label>

            {transactionForm.type === "TRANSFER" ? (
              <label>
                目标账户
                <select
                  value={transactionForm.targetAccountId}
                  onChange={(event) => onFieldChange("targetAccountId", event.target.value)}
                  required
                >
                  <option value="">请选择目标账户</option>
                  {bootstrap.accounts.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </label>
            ) : transactionForm.splitMode ? (
              <div className="split-editor span-2">
                <div className="split-head">
                  <strong>分账明细</strong>
                  <button type="button" className="ghost-button" onClick={onAddSplit}>
                    <Plus size={16} />
                    添加分账
                  </button>
                </div>
                {transactionForm.splits.map((split, index) => (
                  <div className="split-row" key={`${index}-${split.categoryId}`}>
                    <select
                      value={split.categoryId}
                      onChange={(event) =>
                        onSplitChange(index, "categoryId", event.target.value)
                      }
                    >
                      {currentCategories.map((item) => (
                        <option key={item.id} value={item.id}>
                          {item.name}
                        </option>
                      ))}
                    </select>
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={split.amount}
                      onChange={(event) =>
                        onSplitChange(index, "amount", event.target.value)
                      }
                      placeholder="分账金额"
                    />
                    <input
                      value={split.note}
                      onChange={(event) => onSplitChange(index, "note", event.target.value)}
                      placeholder="分账备注"
                    />
                    <button
                      type="button"
                      className="icon-button danger"
                      onClick={() => onRemoveSplit(index)}
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
                <p className="split-hint">
                  分账合计 {formatCurrency(splitTotal)} / 流水总额{" "}
                  {formatCurrency(transactionForm.amount || 0)}
                </p>
              </div>
            ) : (
              <label>
                分类
                <select
                  value={transactionForm.categoryId}
                  onChange={(event) => onFieldChange("categoryId", event.target.value)}
                  required
                >
                  {currentCategories.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </label>
            )}

            <label>
              商户
              <input
                value={transactionForm.merchant}
                onChange={(event) => onFieldChange("merchant", event.target.value)}
                placeholder="例如 瑞幸 / 京东 / 公司发薪"
              />
            </label>

            <label>
              备注
              <input
                value={transactionForm.note}
                onChange={(event) => onFieldChange("note", event.target.value)}
                placeholder="记录收支场景"
              />
            </label>

            <label className="span-2">
              标签
              <input
                value={transactionForm.tagsInput}
                onChange={(event) => onFieldChange("tagsInput", event.target.value)}
                placeholder="例如 通勤,固定,健康"
              />
            </label>

            <button className="primary-button span-2" type="submit">
              {editingTransactionId ? "保存修改" : "写入账本"}
            </button>
          </form>
        </article>
      ) : null}

      {showTransactions ? (
        <article id="transactions" className="panel panel-wide">
          <div className="panel-head">
            <div>
              <p className="eyebrow">Transactions</p>
              <h3>流水中心</h3>
            </div>
            <button type="button" className="ghost-button" onClick={onExport}>
              <Download size={16} />
              导出当前筛选
            </button>
          </div>

          <div className="toolbar">
            <label className="search-box">
              <Search size={16} />
              <input
                value={filters.keyword}
                onChange={(event) => onFilterChange("keyword", event.target.value)}
                placeholder="搜索商户、备注、标签"
              />
            </label>

            <select
              value={filters.type}
              onChange={(event) => onFilterChange("type", event.target.value)}
            >
              <option value="">全部类型</option>
              <option value="EXPENSE">支出</option>
              <option value="INCOME">收入</option>
              <option value="TRANSFER">转账</option>
            </select>

            <select
              value={filters.categoryId}
              onChange={(event) => onFilterChange("categoryId", event.target.value)}
            >
              <option value="">全部分类</option>
              {allCategories.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
          </div>

          {popularTags.length ? (
            <div className="tag-shortcuts">
              {popularTags.map((tag) => (
                <button
                  type="button"
                  key={tag}
                  className="tag-chip"
                  onClick={() => onFilterChange("keyword", tag)}
                >
                  #{tag}
                </button>
              ))}
            </div>
          ) : null}

          <div className="transaction-list">
            {transactions.map((item) => (
              <article className="transaction-row" key={item.id}>
                <div className="transaction-main">
                  <div
                    className="transaction-tag"
                    style={{
                      background:
                        item.categoryColor || item.sourceAccountColor || "var(--accent)",
                    }}
                  />
                  <div>
                    <h4>{transactionTitle(item)}</h4>
                    <p>
                      {item.transactionDate} · {item.sourceAccountName}
                      {item.targetAccountName ? ` -> ${item.targetAccountName}` : ""}
                      {item.categoryName ? ` · ${item.categoryName}` : ""}
                    </p>
                    {item.note ? <p>{item.note}</p> : null}
                    {item.splitSummary ? (
                      <p className="split-caption">分账: {item.splitSummary}</p>
                    ) : null}
                    {item.tags?.length ? (
                      <div className="row-tags">
                        {item.tags.map((tag) => (
                          <span className="tag-pill" key={`${item.id}-${tag}`}>
                            #{tag}
                          </span>
                        ))}
                      </div>
                    ) : null}
                  </div>
                </div>

                <div className="transaction-meta">
                  <strong className={item.type === "EXPENSE" ? "negative" : "positive"}>
                    {item.type === "EXPENSE" ? "-" : "+"}
                    {formatCurrency(item.amount)}
                  </strong>
                  <span className="type-badge">{typeLabels[item.type]}</span>
                  <div className="row-actions">
                    <button
                      type="button"
                      className="icon-button"
                      onClick={() => onEdit(item)}
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      type="button"
                      className="icon-button danger"
                      onClick={() => onDelete(item.id)}
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </article>
      ) : null}
    </>
  );
}
