import {
  Component,
  input,
  output,
  signal,
  computed,
  ElementRef,
  HostListener,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon, LucideChevronDown, LucideCheck, LucideSearch } from '@lucide/angular';

export interface SelectOption<T = any> {
  value: T;
  label: string;
  badge?: string;
  badgeClass?: string;
}

@Component({
  selector: 'app-custom-select',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  templateUrl: './custom-select.html',
})
export class CustomSelect {
  private elementRef = inject(ElementRef);

  readonly LucideChevronDown = LucideChevronDown;
  readonly LucideCheck = LucideCheck;
  readonly LucideSearch = LucideSearch;

  readonly options = input<SelectOption[]>([]);
  readonly selectedValue = input<any>(null);
  readonly placeholder = input<string>('Sélectionner...');
  readonly icon = input<any>(null);
  readonly iconClass = input<string>('text-blue-600');
  readonly searchable = input<boolean>(false);
  readonly disabled = input<boolean>(false);

  readonly valueChange = output<any>();

  isOpen = signal<boolean>(false);
  searchQuery = signal<string>('');

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  toggleDropdown(): void {
    if (this.disabled()) return;
    this.isOpen.update((v) => !v);
    this.searchQuery.set('');
  }

  selectOption(value: any): void {
    this.valueChange.emit(value);
    this.isOpen.set(false);
  }

  readonly selectedOption = computed(() => {
    const val = this.selectedValue();
    return this.options().find((o) => o.value === val) || null;
  });

  readonly filteredOptions = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    if (!q) return this.options();
    return this.options().filter((o) => o.label.toLowerCase().includes(q));
  });
}
