import {
  Component,
  ElementRef,
  HostListener,
  inject,
  input,
  model,
  output,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideChevronDown, LucideCheck } from '@lucide/angular';

export interface DropdownItem {
  label: string;
  value: string;
}

@Component({
  selector: 'app-custom-dropdown',
  standalone: true,
  imports: [CommonModule, LucideChevronDown, LucideCheck],
  templateUrl: './custom-dropdown.html',
})
export class CustomDropdown {
  private elementRef = inject(ElementRef);

  /** List of selectable options (strings or {label, value} objects) */
  readonly options = input<readonly (string | DropdownItem)[]>([]);

  /** Placeholder when no option is selected */
  readonly placeholder = input<string>('Sélectionner une option...');

  /** Currently selected option value (two-way binding) */
  readonly value = model<string | null | undefined>(null);

  /** Whether the dropdown is disabled */
  readonly disabled = input<boolean>(false);

  /** Optional prefix label displayed inside the trigger (e.g. 'Statut : ') */
  readonly prefix = input<string>('');

  /** In badge mode, selection emits the item and resets the trigger display */
  readonly badgeMode = input<boolean>(false);

  /** Emits when an option is selected */
  readonly selectionChange = output<string>();

  /** Popover open state */
  readonly isOpen = signal<boolean>(false);

  getOptionLabel(opt: string | DropdownItem): string {
    return typeof opt === 'string' ? opt : opt.label;
  }

  getOptionValue(opt: string | DropdownItem): string {
    return typeof opt === 'string' ? opt : opt.value;
  }

  getDisplayLabel(): string {
    if (this.badgeMode()) return this.placeholder();
    const val = this.value();
    if (!val) return this.placeholder();
    const found = this.options().find((o) => this.getOptionValue(o) === val);
    return found ? this.getOptionLabel(found) : val;
  }

  isSelected(opt: string | DropdownItem): boolean {
    return !this.badgeMode() && this.value() === this.getOptionValue(opt);
  }

  toggle() {
    if (this.disabled()) return;
    this.isOpen.update((v) => !v);
  }

  close() {
    this.isOpen.set(false);
  }

  selectOption(opt: string | DropdownItem) {
    const val = this.getOptionValue(opt);
    if (!this.badgeMode()) {
      this.value.set(val);
    }
    this.selectionChange.emit(val);
    this.close();
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.close();
    }
  }
}
