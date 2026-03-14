import { Pencil, RefreshCcw, Trash2 } from "lucide-react";
import {
  categoriesByType,
  formatCurrency,
  frequencyLabels,
  typeLabels,
} from "../helpers";

export default function RecurringSection({
  bootstrap,
  recurringForm,
  recurringRules,
  editingRuleId,
  onFieldChange,
  onSubmit,
  onProcess,
  onEdit,
  onDelete,
  onResetEdit,
}) {
  const currentCategories = categoriesByType(bootstrap, recurringForm.type);

  return (
    <article id="recurring" className="panel panel-wide">
      <div className="panel-head">
        <div>
          <p className="eyebrow">Recurring Tasks</p>
          <h3>周期任务编排</h3>
          <p className="panel-subcopy">像后台定时任务一样管理自动记账规则与执行周期。</p>
        </div>
        <div className="panel-actions">
          <button type="button" className="ghost-button" onClick={onProcess}>
            <RefreshCcw size={16} />
            立即处理到期规则
          </button>
          {editingRuleId ? (
            <button type="button" className="ghost-button" onClick={onResetEdit}>
              取消编辑
            </button>
          ) : null}
        </div>
      </div>

      <div className="recurring-layout">
        <form className="form-grid" onSubmit={onSubmit}>
          <label className="span-2">
            规则名称
            <input
              value={recurringForm.title}
              onChange={(event) => onFieldChange("title", event.target.value)}
              placeholder="例如 房租自动记账"
              required
            />
          </label>

          <label>
            类型
            <select
              value={recurringForm.type}
              onChange={(event) => onFieldChange("type", event.target.value)}
            >
              <option value="EXPENSE">支出</option>
              <option value="INCOME">收入</option>
              <option value="TRANSFER">转账</option>
            </select>
          </label>

          <label>
            频率
            <select
              value={recurringForm.frequency}
              onChange={(event) => onFieldChange("frequency", event.target.value)}
            >
              <option value="MONTHLY">每月</option>
              <option value="WEEKLY">每周</option>
            </select>
          </label>

          <label>
            开始日期
            <input
              type="date"
              value={recurringForm.startDate}
              onChange={(event) => onFieldChange("startDate", event.target.value)}
              required
            />
          </label>

          <label>
            下次执行
            <input
              type="date"
              value={recurringForm.nextRunDate}
              onChange={(event) => onFieldChange("nextRunDate", event.target.value)}
              required
            />
          </label>

          <label>
            结束日期
            <input
              type="date"
              value={recurringForm.endDate}
              onChange={(event) => onFieldChange("endDate", event.target.value)}
            />
          </label>

          <label>
            付款账户
            <select
              value={recurringForm.sourceAccountId}
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

          {recurringForm.type === "TRANSFER" ? (
            <label>
              目标账户
              <select
                value={recurringForm.targetAccountId}
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
          ) : (
            <label>
              分类
              <select
                value={recurringForm.categoryId}
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
            金额
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={recurringForm.amount}
              onChange={(event) => onFieldChange("amount", event.target.value)}
              required
            />
          </label>

          <label>
            商户
            <input
              value={recurringForm.merchant}
              onChange={(event) => onFieldChange("merchant", event.target.value)}
            />
          </label>

          <label>
            备注
            <input
              value={recurringForm.note}
              onChange={(event) => onFieldChange("note", event.target.value)}
            />
          </label>

          <label className="span-2">
            标签
            <input
              value={recurringForm.tagsInput}
              onChange={(event) => onFieldChange("tagsInput", event.target.value)}
              placeholder="例如 固定,自动"
            />
          </label>

          <label className="checkbox-field span-2">
            <input
              type="checkbox"
              checked={recurringForm.active}
              onChange={(event) => onFieldChange("active", event.target.checked)}
            />
            启用规则
          </label>

          <button className="primary-button span-2" type="submit">
            {editingRuleId ? "保存规则" : "创建规则"}
          </button>
        </form>

        <div className="rule-list">
          {recurringRules.map((item) => (
            <article className="rule-card" key={item.id}>
              <div className="rule-head">
                <div>
                  <h4>{item.title}</h4>
                  <p>
                    {frequencyLabels[item.frequency]} · {typeLabels[item.type]}
                  </p>
                </div>
                <span className={item.active ? "status-pill active" : "status-pill"}>
                  {item.active ? "启用中" : "已停用"}
                </span>
              </div>
              <p>
                下次执行 {item.nextRunDate} · {item.sourceAccountName}
                {item.targetAccountName ? ` -> ${item.targetAccountName}` : ""}
                {item.categoryName ? ` · ${item.categoryName}` : ""}
              </p>
              <strong>{formatCurrency(item.amount)}</strong>
              <div className="row-tags">
                {(item.labels || "")
                  .split(",")
                  .filter(Boolean)
                  .map((tag) => (
                    <span className="tag-pill" key={`${item.id}-${tag}`}>
                      #{tag}
                    </span>
                  ))}
              </div>
              <div className="row-actions">
                <button type="button" className="icon-button" onClick={() => onEdit(item)}>
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
            </article>
          ))}
        </div>
      </div>
    </article>
  );
}
