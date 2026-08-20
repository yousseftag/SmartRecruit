import { LucideIconInput } from '@lucide/angular';

export interface NavItem {
  label: string;
  route: string;
  icon: LucideIconInput;
  roles?: ('HR_ADMIN' | 'RECRUITER' | 'VIEWER')[];
}

