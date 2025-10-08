# Commerce Services API Documentation

This document provides detailed information about the Commerce Services REST API endpoints.

## Base URL

```
http://localhost:8080/api/v1
```

## Endpoints

### Checkout Service

#### `POST /checkout/process`

Processes a checkout with SAGA orchestration. This is the main entry point for the e-commerce flow.

**Request Body:**
```json
{
  "customerId": "a8b8c8d8-e8f8-g8h8-i8j8-k8l8m8n8o8p8",
  "items": [
    {
      "productId": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
      "quantity": 1
    }
  ],
  "paymentMethod": "CREDIT_CARD"
}
```

**Response:**
- `200 OK`: Checkout successful
- `400 Bad Request`: Checkout failed

### Inventory Service

#### `GET /inventory/products/{productId}`

Get inventory for a specific product.

#### `POST /inventory/reserve`

Reserve stock for an order.

### Order Service

#### `POST /orders`

Create a new order.

### Payment Service

#### `POST /payments/process`

Process a payment for an order.

## Data Models

### CheckoutRequest

| Field | Type | Description |
|---|---|---|
| `customerId` | UUID | The ID of the customer |
| `items` | List<CheckoutItem> | The items to be purchased |
| `paymentMethod` | String | The payment method to be used |

### CheckoutItem

| Field | Type | Description |
|---|---|---|
| `productId` | UUID | The ID of the product |
| `quantity` | Integer | The quantity of the product |

## Error Responses

All error responses follow this format:

```json
{
  "timestamp": "2023-10-27T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message",
  "path": "/api/v1/checkout/process"
}
```

