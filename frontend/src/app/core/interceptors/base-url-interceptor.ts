import { HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { InjectionToken, inject } from '@angular/core';

export const BASE_URL = new InjectionToken<string>('BASE_URL');

export function hasHttpScheme(url: string) {
  return new RegExp('^http(s)?://', 'i').test(url);
}

const API_PREFIXES = ['/auth/', '/user', '/api/'];

function isApiCall(url: string) {
  return API_PREFIXES.some(prefix => url.startsWith(prefix));
}

export function baseUrlInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const baseUrl = inject(BASE_URL, { optional: true });

  const hasScheme = (url: string) => baseUrl && hasHttpScheme(url);

  const prependBaseUrl = (url: string) =>
    [baseUrl?.replace(/\/$/g, ''), url.replace(/^\.?\//, '')].filter(val => val).join('/');

  if (hasScheme(req.url) === false && isApiCall(req.url)) {
    return next(req.clone({ url: prependBaseUrl(req.url) }));
  }

  return next(req);
}
