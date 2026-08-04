import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { Header } from './header';
import { AuthService } from '../../core/auth/auth.service';

const mockAuthService = {
  isAuthenticated: signal(true),
  isAdmin: signal(true),
  hasRole: () => true,
  getUserProfile: () => ({
    firstName: 'Test',
    lastName: 'User',
    fullName: 'Test User',
    email: 'test@example.com',
    preferredUsername: 'testuser',
  }),
  logout: async () => {},
  manageAccount: () => {},
  changePassword: () => {},
};

describe('Header', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([]), { provide: AuthService, useValue: mockAuthService }],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
