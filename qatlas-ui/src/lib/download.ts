/** Saves a Blob to the user's machine via a temporary <a download> link. */
export function downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    // give the browser a moment to start the download before revoking
    setTimeout(() => URL.revokeObjectURL(url), 1_000);
}

/** Reads the file name from a Content-Disposition header, or returns the fallback. */
export function filenameFromContentDisposition(header: unknown, fallback: string): string {
    if (typeof header !== 'string') return fallback;
    const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header);
    if (utf8) {
        try {
            return decodeURIComponent(utf8[1].trim());
        } catch {
            /* fall through */
        }
    }
    const plain = /filename="?([^";]+)"?/i.exec(header);
    return plain ? plain[1].trim() : fallback;
}

/** When responseType is 'blob', axios also returns error bodies as a Blob. */
export async function readBlobError(error: unknown): Promise<string | null> {
    const data = (error as { response?: { data?: unknown } })?.response?.data;
    if (data instanceof Blob) {
        try {
            const text = await data.text();
            try {
                const json = JSON.parse(text);
                return json.message ?? json.error ?? text;
            } catch {
                return text || null;
            }
        } catch {
            return null;
        }
    }
    return null;
}
