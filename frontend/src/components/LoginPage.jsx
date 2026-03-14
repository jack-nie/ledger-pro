import { Cloud, ShieldCheck } from "lucide-react";

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
        <div className="login-hero-badge">
          <Cloud size={18} />
          腾讯云控制台风格
        </div>
        <p className="eyebrow">Ledger Pro Console</p>
        <h1>把记账、预算、周期规则和账户视图统一放进一套后台控制台。</h1>
        <p className="login-copy">
          当前界面按云控制台重新布局，强调导航分区、资源卡片、白色业务面板和腾讯云式蓝色主操作。
        </p>

        <div className="login-feature-grid">
          <div className="login-feature-card">
            <strong>模块化导航</strong>
            <p>像 CVM 控制台一样按功能切换，不把所有内容堆在同一页。</p>
          </div>
          <div className="login-feature-card">
            <strong>统一资产视图</strong>
            <p>本月现金流、预算执行和账户余额集中在后台总览页展示。</p>
          </div>
          <div className="login-feature-card">
            <strong>本地演示账户</strong>
            <p>
              默认账号 <code>demo</code> / <code>123456</code>
            </p>
          </div>
        </div>
      </section>

      <form className="login-card" onSubmit={onSubmit}>
        <div className="login-head">
          <div className="login-head-icon">
            <ShieldCheck size={20} />
          </div>
          <div>
            <h2>登录财务控制台</h2>
            <p>进入后可查看总览、流水、预算、周期规则和账户视图。</p>
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
          {authSubmitting ? "登录中..." : "登录控制台"}
        </button>
      </form>
    </div>
  );
}
