import { useState } from 'react';
import type { OrderRequest, OrderResult } from '../types/order';

interface UseOrderProcessorReturn {
  result: OrderResult | null;
  isLoading: boolean;
  error: string | null;
  processOrder: (request: OrderRequest) => Promise<void>;
  processOrderOffline: () => Promise<void>;
  reset: () => void;
}

const OFFLINE_MOCK_RESULT: OrderResult = {
  orderId: 'a5a42903-2156-4560-82c4-9ee105d0e319',
  clientId: 'client-001',
  clientName: 'ACME Industrial',
  items: [
    {
      productName: 'caja de papel A4',
      quantity: 12,
      unitPrice: 14.5,
      totalPrice: 174,
    },
    {
      productName: 'bolígrafo azul',
      quantity: 30,
      unitPrice: 1.8,
      totalPrice: 54,
    },
    {
      productName: 'cuaderno tamaño carta',
      quantity: 6,
      unitPrice: 4.25,
      totalPrice: 25.5,
    },
    {
      productName: 'engrapadora',
      quantity: 3,
      unitPrice: 9.9,
      totalPrice: 29.7,
    },
  ],
  totalAmount: 283.2,
  status: 'PENDING',
  createdAt: '2026-03-17T17:47:32.154324125',
};

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Custom Hook — encapsulates ALL async logic for order processing.
 *
 * The component (SmartOrder.tsx) remains a pure presentational layer:
 * it receives state and callbacks from this hook, with zero fetch logic.
 *
 * Uses import.meta.env.PUBLIC_API_URL so the build-time URL is injected
 * by the Astro build process (or .env file locally).
 */
export function useOrderProcessor(): UseOrderProcessorReturn {
  const [result, setResult] = useState<OrderResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const apiUrl = import.meta.env.PUBLIC_API_URL ?? 'http://localhost:8080';
  const apiKey = import.meta.env.PUBLIC_API_KEY ?? '';
  const endpoint = `${apiUrl}/api/v1/orders/ai-ingest`;

  const processOrder = async (request: OrderRequest): Promise<void> => {
    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(apiKey && { 'X-API-Key': apiKey }),
        },
        body: JSON.stringify(request),
      });

      if (!response.ok) {
        const errorBody = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorBody.message ?? `HTTP ${response.status}`);
      }

      const data: OrderResult = await response.json();
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Ocurrió un error inesperado');
    } finally {
      setIsLoading(false);
    }
  };

  const processOrderOffline = async (): Promise<void> => {
    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      await wait(3000);
      setResult(OFFLINE_MOCK_RESULT);
    } catch {
      setError('Ocurrio un error inesperado en modo offline');
    } finally {
      setIsLoading(false);
    }
  };

  const reset = () => {
    setResult(null);
    setError(null);
  };

  return { result, isLoading, error, processOrder, processOrderOffline, reset };
}
