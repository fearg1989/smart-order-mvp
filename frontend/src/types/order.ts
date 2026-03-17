export interface OrderItemResult {
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface OrderResult {
  orderId: string;
  clientId: string;
  clientName: string;
  items: OrderItemResult[];
  totalAmount: number;
  status: string;
  createdAt: string;
}

export interface OrderRequest {
  rawText: string;
  clientId: string;
  clientName: string;
  clientEmail: string;
}

export interface ErrorResponse {
  message: string;
}
