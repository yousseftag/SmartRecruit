import { Component, AfterViewInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { createIcons, icons } from 'lucide';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-slate-50 font-sans p-4">
      <div
        class="bg-white p-8 md:p-12 rounded-2xl shadow-xl max-w-md w-full text-center border border-slate-200"
      >
        <div
          class="inline-flex items-center justify-center w-20 h-20 bg-red-50 text-red-500 rounded-full mb-6"
        >
          <i data-lucide="alert-circle" class="w-10 h-10"></i>
        </div>

        <h1
          class="text-7xl font-extrabold text-transparent bg-clip-text bg-gradient-to-br from-blue-600 to-blue-400 mb-2 leading-none"
        >
          404
        </h1>

        <h2 class="text-2xl font-bold text-slate-900 mb-4">Page introuvable</h2>

        <p class="text-slate-500 mb-8 leading-relaxed">
          Oups ! La page que vous recherchez semble avoir été supprimée, déplacée ou n'a jamais
          existé.
        </p>

        <a
          routerLink="/dashboard"
          class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-6 rounded-full transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-blue-500/30"
        >
          <i data-lucide="arrow-left" class="w-5 h-5"></i>
          Tableau de bord
        </a>
      </div>
    </div>
  `,
})
export class NotFoundComponent implements AfterViewInit {
  ngAfterViewInit() {
    createIcons({ icons: icons as any });
  }
}
