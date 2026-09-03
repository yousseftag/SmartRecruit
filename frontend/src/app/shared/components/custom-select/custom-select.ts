import {
  Component,
  input,
  output,
  signal,
  computed,
  ElementRef,
  HostListener,
  inject,
  forwardRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { LucideDynamicIcon, LucideChevronDown, LucideCheck, LucideSearch } from '@lucide/angular';

export interface SelectOption<T = any> {
  value: T;
  label: string;
  description?: string;
  badge?: string;
  badgeClass?: string;
}

@Component({
  selector: 'app-custom-select',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideDynamicIcon],
  templateUrl: './custom-select.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CustomSelect),
      multi: true,
    },
  ],
})
export class CustomSelect implements ControlValueAccessor {
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
  readonly disabledInput = input<boolean>(false, { alias: 'disabled' });

  readonly valueChange = output<any>();

  isOpen = signal<boolean>(false);
  searchQuery = signal<string>('');

  // Internal value from ControlValueAccessor
  internalValue = signal<any>(null);
  formDisabled = signal<boolean>(false);

  // Callbacks for CVA
  private onChange: (val: any) => void = () => {};
  private onTouched: () => void = () => {};

  readonly disabled = computed(() => this.disabledInput() || this.formDisabled());

  readonly effectiveValue = computed(() => {
    const propVal = this.selectedValue();
    if (propVal !== null && propVal !== undefined) {
      return propVal;
    }
    return this.internalValue();
  });

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
    this.onTouched();
  }

  selectOption(value: any): void {
    this.internalValue.set(value);
    this.onChange(value);
    this.onTouched();
    this.valueChange.emit(value);
    this.isOpen.set(false);
  }

  readonly selectedOption = computed(() => {
    const val = this.effectiveValue();
    return this.options().find((o) => o.value === val) || null;
  });

  readonly filteredOptions = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    if (!q) return this.options();
    return this.options().filter((o) => o.label.toLowerCase().includes(q));
  });

  // ControlValueAccessor Implementation
  writeValue(obj: any): void {
    this.internalValue.set(obj);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }
}

// Export alias for backward compatibility with CustomSelectComponent naming
export { CustomSelect as CustomSelectComponent };
