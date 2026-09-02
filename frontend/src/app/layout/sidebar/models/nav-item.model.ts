import { LucideIconInput } from '@lucide/angular';
import { UserRole } from '../../../core/models/user.model';

export interface NavItem {
  label: string;
  route: string;
  icon: LucideIconInput;
  roles?: (UserRole | string)[];
}
