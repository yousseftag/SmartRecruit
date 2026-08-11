import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideSearch,
  LucidePlus,
  LucidePencil,
  LucideTrash2,
  LucideChevronLeft,
  LucideChevronRight,
} from '@lucide/angular';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserResponse } from '../../../core/models/user.model';
import { EditProfile } from '../../edit-profile/edit-profile';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideSearch,
    LucidePlus,
    LucidePencil,
    LucideTrash2,
    LucideChevronLeft,
    LucideChevronRight,
    EditProfile,
  ],
  templateUrl: './user-management.html',
})
export class UserManagement implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);

  allUsers = signal<UserResponse[]>([]);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');

  searchQuery = signal<string>('');
  selectedRole = signal<string>('ALL');
  pageSize = signal<number>(10);
  currentPage = signal<number>(1);

  currentUserSub = signal<string>('');

  isOwnProfileModalOpen = signal<boolean>(false);

  // Computed signal to filter and paginate
  filteredUsers = computed(() => {
    let users = this.allUsers();

    // Filter by role
    const role = this.selectedRole();
    if (role !== 'ALL') {
      users = users.filter((u) => u.role === role);
    }

    // Filter by search query (Tokenized approach)
    const query = this.searchQuery().trim().toLowerCase();
    if (query) {
      const searchTerms = query.split(' ').filter((term) => term.trim() !== '');
      users = users.filter((u) => {
        const searchableText =
          `${u.username || ''} ${u.firstName || ''} ${u.lastName || ''}`.toLowerCase();
        return searchTerms.every((term) => searchableText.includes(term));
      });
    }

    return users;
  });

  paginatedUsers = computed(() => {
    const users = this.filteredUsers();
    const start = (this.currentPage() - 1) * this.pageSize();
    const end = start + this.pageSize();
    return users.slice(start, end);
  });

  totalPages = computed(() => {
    return Math.max(1, Math.ceil(this.filteredUsers().length / this.pageSize()));
  });

  ngOnInit() {
    const profile = this.authService.getUserProfile();
    if (profile && profile.sub) {
      this.currentUserSub.set(profile.sub);
    }
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading.set(true);
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.allUsers.set(users);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set('Erreur lors du chargement des utilisateurs.');
        this.isLoading.set(false);
      },
    });
  }

  formatRole(role: string): string {
    switch (role) {
      case 'HR_ADMIN':
        return 'Admin RH';
      case 'RECRUITER':
        return 'Recruteur';
      default:
        return 'Consultation';
    }
  }

  onSearchChange(event: any) {
    this.searchQuery.set(event.target.value);
    this.currentPage.set(1);
  }

  onRoleChange(event: any) {
    this.selectedRole.set(event.target.value);
    this.currentPage.set(1);
  }

  onPageSizeChange(event: any) {
    this.pageSize.set(Number(event.target.value));
    this.currentPage.set(1);
  }

  prevPage() {
    if (this.currentPage() > 1) {
      this.currentPage.update((p) => p - 1);
    }
  }

  nextPage() {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update((p) => p + 1);
    }
  }

  openCreateModal() {}

  openEditModal(user: UserResponse) {
    if (user.keycloakSub === this.currentUserSub()) {
      this.isOwnProfileModalOpen.set(true);
    } else {
    }
  }

  openDeleteModal(user: UserResponse) {}

  onOwnProfileModalClose(success: boolean) {
    this.isOwnProfileModalOpen.set(false);
    if (success) {
      this.loadUsers();
    }
  }
}
