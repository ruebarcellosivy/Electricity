import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Frontend mirrors of the backend Bean Validation rules, so users get instant feedback. */
export class CustomValidators {
  static consumerNumber(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return /^\d{13}$/.test(control.value) ? null : { consumerNumber: true };
    };
  }

  static nameOnly(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return /^[A-Za-z ]+$/.test(control.value) ? null : { nameOnly: true };
    };
  }

  static mobileNumber(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return /^[6-9]\d{9}$/.test(control.value) ? null : { mobileNumber: true };
    };
  }

  static strongPassword(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value: string = control.value || '';
      if (!value) return null;
      const hasUpper = /[A-Z]/.test(value);
      const hasLower = /[a-z]/.test(value);
      const hasSpecial = /[^a-zA-Z0-9]/.test(value);
      const longEnough = value.length >= 8;
      return hasUpper && hasLower && hasSpecial && longEnough ? null : { strongPassword: true };
    };
  }

  static cardNumber(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return /^\d{16}$/.test(control.value) ? null : { cardNumber: true };
    };
  }

  static expiryDate(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value: string = control.value || '';
      if (!value) return null;
      const match = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(value);
      if (!match) return { expiryFormat: true };
      const month = Number(match[1]);
      const year = 2000 + Number(match[2]);
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;
      if (year < currentYear || (year === currentYear && month < currentMonth)) {
        return { expired: true };
      }
      return null;
    };
  }

  static cvv(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return /^\d{3,4}$/.test(control.value) ? null : { cvv: true };
    };
  }

  /** Class-level validator: applies a "mismatch" error on `matchControlName` when it differs from `controlName`. */
  static matchFields(controlName: string, matchControlName: string): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const control = group.get(controlName);
      const matchControl = group.get(matchControlName);
      if (!control || !matchControl) return null;
      if (matchControl.errors && !matchControl.errors['mismatch']) return null;

      if (control.value !== matchControl.value) {
        matchControl.setErrors({ ...matchControl.errors, mismatch: true });
      } else if (matchControl.errors) {
        const { mismatch, ...rest } = matchControl.errors;
        matchControl.setErrors(Object.keys(rest).length ? rest : null);
      }
      return null;
    };
  }

  static dateNotBefore(fromControlName: string, toControlName: string): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const from = group.get(fromControlName)?.value;
      const to = group.get(toControlName)?.value;
      const toControl = group.get(toControlName);
      if (!from || !to || !toControl) return null;

      const fromDate = new Date(from);
      const toDate = new Date(to);
      if (toDate < fromDate) {
        toControl.setErrors({ ...toControl.errors, dateRange: true });
      } else if (toControl.errors) {
        const { dateRange, ...rest } = toControl.errors;
        toControl.setErrors(Object.keys(rest).length ? rest : null);
      }
      return null;
    };
  }
}
