import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatCurrency } from "../helpers";

export default function AccountsPanel({ accounts }) {
  return (
    <article id="accounts" className="panel">
      <div className="panel-head">
        <div>
          <p className="eyebrow">Accounts</p>
          <h3>账户分布</h3>
        </div>
      </div>

      <div className="chart-wrap small">
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={accounts || []}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="name" tickLine={false} axisLine={false} />
            <YAxis tickLine={false} axisLine={false} tickFormatter={formatCurrency} />
            <Tooltip formatter={(value) => formatCurrency(value)} />
            <Bar dataKey="balance" radius={[12, 12, 0, 0]}>
              {(accounts || []).map((item) => (
                <Cell key={item.id} fill={item.colorHex || "#0f766e"} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="account-grid">
        {(accounts || []).map((item) => (
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
  );
}
