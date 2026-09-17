import type { TestStepAttachment } from '../types/domain';

const IMAGE_EXTENSIONS = new Set([
  'png',
  'jpg',
  'jpeg',
  'gif',
  'webp',
  'bmp',
]);

export function attachmentUrl(relativePath?: string): string | undefined {
  if (!relativePath) return undefined;

  const cleaned = relativePath
      .replace(/^(\.\.\/)+/, '')
      .replace(/^\/+/, '');

  const base = import.meta.env.VITE_API_BASE_URL ?? '';

  return `${base}/attachment/${cleaned}`;
}

export function attachmentExtension(
    attachment?: Pick<TestStepAttachment, 'fileName' | 'attachmentRelativePath'>
): string {
  const source =
      attachment?.fileName ||
      attachment?.attachmentRelativePath ||
      '';

  const cleanSource = source.split('?')[0].split('#')[0];
  const lastPart = cleanSource.split('/').pop() ?? '';
  const dotIndex = lastPart.lastIndexOf('.');

  if (dotIndex === -1 || dotIndex === lastPart.length - 1) {
    return '';
  }

  return lastPart.substring(dotIndex + 1).toLowerCase();
}

export function isImageAttachment(
    attachment?: Pick<TestStepAttachment, 'fileName' | 'attachmentRelativePath'>
): boolean {
  return IMAGE_EXTENSIONS.has(attachmentExtension(attachment));
}

export function attachmentDisplayType(
    attachment?: Pick<TestStepAttachment, 'fileName' | 'attachmentRelativePath'>
): string {
  const extension = attachmentExtension(attachment);

  if (!extension) {
    return 'FILE';
  }

  return extension.toUpperCase();
}

export function attachmentFileName(
    attachment?: Pick<TestStepAttachment, 'fileName' | 'attachmentRelativePath'>
): string {
  if (attachment?.fileName) {
    return attachment.fileName;
  }

  const relativePath = attachment?.attachmentRelativePath;

  if (!relativePath) {
    return 'Attachment';
  }

  return relativePath.split('/').pop() || 'Attachment';
}