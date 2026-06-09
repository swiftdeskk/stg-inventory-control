/* Sidebar toggle */
document.getElementById("sidebarCollapse")
  ?.addEventListener("click", () => {
    document.getElementById("sidebar").classList.toggle("active");
    const overlay = document.getElementById("sidebar-overlay");
    if (overlay) overlay.classList.toggle("active");
  });

document.getElementById("sidebarCollapseClose")
  ?.addEventListener("click", () => {
    document.getElementById("sidebar").classList.remove("active");
    const overlay = document.getElementById("sidebar-overlay");
    if (overlay) overlay.classList.remove("active");
  });

document.getElementById("sidebar-overlay")
  ?.addEventListener("click", () => {
    document.getElementById("sidebar").classList.remove("active");
    document.getElementById("sidebar-overlay").classList.remove("active");
  });

/* Dark / Light mode toggle */
const THEME_KEY = "techstore-theme";

function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);
  const icon = document.getElementById("theme-icon");
  if (!icon) return;
  if (theme === "dark") {
    icon.className = "fa-solid fa-sun";
    icon.closest("button").setAttribute("title", "Cambiar a modo claro");
  } else {
    icon.className = "fa-solid fa-moon";
    icon.closest("button").setAttribute("title", "Cambiar a modo oscuro");
  }
}

(function initTheme() {
  const saved = localStorage.getItem(THEME_KEY) || "light";
  applyTheme(saved);
})();

document.getElementById("btn-theme-toggle")?.addEventListener("click", () => {
  const current = document.documentElement.getAttribute("data-theme") || "light";
  const next = current === "dark" ? "light" : "dark";
  localStorage.setItem(THEME_KEY, next);
  applyTheme(next);
});
