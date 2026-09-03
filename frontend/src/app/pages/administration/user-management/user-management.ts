import {
  Component,
  OnInit,
  inject,
  signal,
  computed,
  HostListener,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideSearch,
  LucidePlus,
  LucidePencil,
  LucideChevronLeft,
  LucideChevronRight,
  LucideChevronDown,
  LucideCheck,
  LucideFilter,
  LucideTrash2,
  LucideCircleCheck,
  LucideAlertTriangle,
  LucideXCircle,
} from '@lucide/angular';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/auth/auth.service';
import {
  UserResponse,
  CreateUserRequest,
  UpdateUserRequest,
} from '../../../core/models/user.model';
import { EditProfile } from '../../edit-profile/edit-profile';
import { CreateUserModalComponent } from './components/create-user-modal/create-user-modal';
import { EditUserModalComponent } from './components/edit-user-modal/edit-user-modal';
import { ConfirmDeleteModalComponent } from '../../../shared/components/confirm-delete-modal/confirm-delete-modal';
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
    LucideChevronDown,
    LucideCheck,
    LucideFilter,
    LucideCircleCheck,
    LucideAlertTriangle,
    LucideXCircle,
    EditProfile,
    CreateUserModalComponent,
    EditUserModalComponent,
    ConfirmDeleteModalComponent,
  ],
  templateUrl: './user-management.html',
})
export class UserManagement implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private elementRef = inject(ElementRef);

  allUsers = signal<UserResponse[]>([]);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');

  searchQuery = signal<string>('');
  selectedRole = signal<string>('ALL');
  isRoleDropdownOpen = signal<boolean>(false);
  isPageSizeDropdownOpen = signal<boolean>(false);
  pageSize = signal<number>(10);
  currentPage = signal<number>(1);

  currentUsername = signal<string>('');

  isOwnProfileModalOpen = signal<boolean>(false);
  isCreateModalOpen = signal<boolean>(false);
  selectedUserForEdit = signal<UserResponse | null>(null);
  selectedUserForDelete = signal<UserResponse | null>(null);

  successMessage = signal<string>('');
  warningMessage = signal<string>('');
  errorToastMessage = signal<string>('');

  selectedRoleLabel = computed(() => {
    switch (this.selectedRole()) {
      case 'HR_ADMIN':
        return 'Admin RH';
      case 'RECRUITER':
        return 'Recruteur';
      case 'VIEWER':
        return 'Consultation';
      default:
        return 'Tous les rôles';
    }
  });

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isRoleDropdownOpen.set(false);
      this.isPageSizeDropdownOpen.set(false);
    }
  }

  showToast(message: string) {
    this.successMessage.set(message);
    setTimeout(() => this.successMessage.set(''), 5000);
  }

  showWarning(message: string) {
    this.warningMessage.set(message);
    setTimeout(() => this.warningMessage.set(''), 7000);
  }

  showErrorToast(message: string) {
    this.errorToastMessage.set(message);
    setTimeout(() => this.errorToastMessage.set(''), 5000);
  }

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
          `${u.username || ''} ${u.firstName || ''} ${u.lastName || ''} ${u.email || ''}`.toLowerCase();
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
    if (profile && profile.preferredUsername) {
      this.currentUsername.set(profile.preferredUsername);
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

  onSearchModelChange(query: string) {
    this.searchQuery.set(query);
    this.currentPage.set(1);
  }

  onSearchChange(event: any) {
    this.searchQuery.set(event.target.value);
    this.currentPage.set(1);
  }

  toggleRoleDropdown(event: Event) {
    event.stopPropagation();
    this.isPageSizeDropdownOpen.set(false);
    this.isRoleDropdownOpen.update((open) => !open);
  }

  selectRole(role: string) {
    this.selectedRole.set(role);
    this.isRoleDropdownOpen.set(false);
    this.currentPage.set(1);
  }

  togglePageSizeDropdown(event: Event) {
    event.stopPropagation();
    this.isRoleDropdownOpen.set(false);
    this.isPageSizeDropdownOpen.update((open) => !open);
  }

  selectPageSize(size: number) {
    this.pageSize.set(size);
    this.isPageSizeDropdownOpen.set(false);
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

  openCreateModal() {
    this.isCreateModalOpen.set(true);
  }

  openEditModal(user: UserResponse) {
    if (user.username === this.currentUsername()) {
      this.isOwnProfileModalOpen.set(true);
    } else {
      this.selectedUserForEdit.set(user);
    }
  }

  openDeleteModal(user: UserResponse) {
    this.selectedUserForDelete.set(user);
  }

  closeCreateModal() {
    this.isCreateModalOpen.set(false);
  }

  onCreateUser(data: CreateUserRequest) {
    this.userService.createUser(data).subscribe({
      next: (response) => {
        this.closeCreateModal();
        this.loadUsers();
        this.showToast('Utilisateur créé avec succès');
        if (response.warning) {
          this.showWarning(response.warning);
        }
      },
      error: (err: HttpErrorResponse) => {
        const errorMsg = err.error?.message || "Erreur lors de la création de l'utilisateur.";
        this.showErrorToast(errorMsg);
      },
    });
  }

  closeEditModal() {
    this.selectedUserForEdit.set(null);
  }

  onEditUser(event: { id: string; data: UpdateUserRequest }) {
    this.userService.updateUser(event.id, event.data).subscribe({
      next: () => {
        this.closeEditModal();
        this.loadUsers();
        this.showToast('Utilisateur mis à jour avec succès');
      },
      error: (err: HttpErrorResponse) => {
        const errorMsg = err.error?.message || 'Erreur lors de la mise à jour.';
        this.showErrorToast(errorMsg);
      },
    });
  }

  closeDeleteModal() {
    this.selectedUserForDelete.set(null);
  }

  onDeleteUser(userId: string) {
    this.userService.deleteUser(userId).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.loadUsers();
        this.showToast('Utilisateur supprimé avec succès');
      },
      error: (err: HttpErrorResponse) => {
        this.closeDeleteModal();
        const errorMsg = err.error?.message || "Erreur lors de la suppression de l'utilisateur.";
        this.showErrorToast(errorMsg);
      },
    });
  }

  onOwnProfileModalClose(success: boolean) {
    this.isOwnProfileModalOpen.set(false);
    if (success) {
      this.authService.profileUpdated.next();
      this.loadUsers();
      this.showToast('Profil mis à jour avec succès');
    }
  }
}
