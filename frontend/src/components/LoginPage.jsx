import { ShieldCheck } from "lucide-react";

export default function LoginPage({
  error,
  loginForm,
  authSubmitting,
  onFieldChange,
  onSubmit,
}) {
  return (
    <div className="login-page">
      <section className="login-hero">
        <p className="eyebrow">Ledger Pro</p>
        <h1>带登录、分账、导出和定时记账的个人财务工作台</h1>
        <p>默认演示账号：`demo` / `123456`。登录 Token 由 Redis 托管。</p>
      </section>

      <form className="login-card" onSubmit={onSubmit}>
        <div className="login-head">
          <ShieldCheck size={20} />
          <div>
            <h2>登录账本</h2>
            <p>进入后可查看仪表盘、流水中心、预算和定时规则。</p>
          </div>
        </div>

        <label>
          用户名
          <input
            value={loginForm.username}
            onChange={(event) => onFieldChange("username", event.target.value)}
            required
          />
        </label>

        <label>
          密码
          <input
            type="password"
            value={loginForm.password}
            onChange={(event) => onFieldChange("password", event.target.value)}
            required
          />
        </label>

        {error ? <div className="message error">{error}</div> : null}

        <button className="primary-button" type="submit" disabled={authSubmitting}>
          {authSubmitting ? "登录中..." : "登录"}
        </button>
      </form>
    </div>
  );
}
