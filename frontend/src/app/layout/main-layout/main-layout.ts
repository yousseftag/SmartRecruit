import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from '../header/header';
import { Sidebar } from '../sidebar/sidebar';
import { Footer } from '../footer/footer';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, Header, Sidebar, Footer],
  template: `
    <!-- Wrapper using the global .app class -->
    <div class="app">
      <app-sidebar></app-sidebar>
      <!-- Main content area using the global .main class -->
      <div class="main">
        <app-header></app-header>
        <!-- Scrollable area for the router outlet -->
        <main style="flex: 1; overflow-y: auto;">
          <router-outlet></router-outlet>
        </main>
        <app-footer></app-footer>
      </div>
    </div>
  `,
})
export class MainLayout {}
