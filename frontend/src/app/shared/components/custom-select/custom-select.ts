import {
  Component,
  ElementRef,
  HostListener,
  Input,
  forwardRef,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { LucideChevronDown, LucideCheck } from '@lucide/angular';

export interface SelectOption {
  value: string;
  label: string;
  description?: string;
}

@Component({
  selector: 'app-custom-select',
  standalone: true,
  imports: [CommonModule, LucideChevronDown, LucideCheck],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CustomSelectComponent),
      multi: true,
    },
  ],
  template: `
    <div class="relative w-full" #container>
      <button
        type="button"
        [disabled]="disabled()"
        (click)="toggleOpen($event)"
        class="w-full px-3 py-2 bg-white border border-line rounded-xl text-xs font-semibold text-ink flex items-center justify-between hover:border-slate/40 focus:border-blue transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        [class.border-blue]="isOpen()"
        style="height: 38px;"
      >
        <span [class.text-muted]="!selectedLabel()">
          {{ selectedLabel() || placeholder }}
        </span>
        <svg
          class="w-3.5 h-3.5 text-slate flex-shrink-0 transition-transform duration-200"
          [class.rotate-180]="isOpen()"
          lucideChevronDown
          size="14"
        ></svg>
      </button>

      @if (isOpen()) {
        <div
          class="absolute top-full left-0 mt-1 w-full bg-white border border-line rounded-xl shadow-lg z-50 py-1 overflow-hidden animate-in fade-in slide-in-from-top-1 duration-150 max-h-56 overflow-y-auto"
          (click)="$event.stopPropagation()"
        >
          @for (option of options; track option.value) {
            <button
              type="button"
              (click)="selectOption(option.value)"
              class="w-full px-3.5 py-2 text-left text-xs flex items-center justify-between hover:bg-bg transition-colors"
              [ngClass]="
                selectedValue() === option.value ? 'text-blue font-bold bg-blue-100/40' : 'text-ink'
              "
            >
              <span>{{ option.label }}</span>
              @if (selectedValue() === option.value) {
                <svg class="w-3.5 h-3.5 text-blue flex-shrink-0" lucideCheck size="14"></svg>
              }
            </button>
          }
        </div>
      }
    </div>
  `,
})
export class CustomSelectComponent implements ControlValueAccessor {
  @Input() options: SelectOption[] = [];
  @Input() placeholder: string = 'Sélectionner...';

  readonly isOpen = signal<boolean>(false);
  readonly selectedValue = signal<string>('');
  readonly disabled = signal<boolean>(false);

  private onChange: (val: string) => void = () => {};
  private onTouched: () => void = () => {};
  private elementRef = inject(ElementRef);

  selectedLabel(): string {
    const found = this.options.find((o) => o.value === this.selectedValue());
    return found ? found.label : '';
  }

  toggleOpen(event: MouseEvent) {
    event.stopPropagation();
    if (!this.disabled()) {
      this.isOpen.update((v) => !v);
    }
  }

  selectOption(val: string) {
    this.selectedValue.set(val);
    this.onChange(val);
    this.onTouched();
    this.isOpen.set(false);
  }

  writeValue(val: string): void {
    this.selectedValue.set(val ?? '');
  }

  registerOnChange(fn: (val: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }
}
