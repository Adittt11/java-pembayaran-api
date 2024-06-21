## API Endpoints

### Items

#### Get All Items

```
GET /items
```

Response:

```json
[
  {
      "id": 1,
      "name": "Basic Plan",
      "price": 1000,
      "type": "plan",
      "is_active": 1
    },
    {
      "id": 2,
      "name": "Premium Plan",
      "price": 2000,
      "type": "plan",
      "is_active": 1
    },
    {
      "id": 3,
      "name": "Additional Storage",
      "price": 500,
      "type": "addon",
      "is_active": 1
    }
]
```

#### Get Active Items

```
GET /items?is_active=true
```

Response:

```json
[
  {
      "id": 1,
      "name": "Basic Plan",
      "price": 1000,
      "type": "plan",
      "is_active": 1
    },
    {
      "id": 2,
      "name": "Premium Plan",
      "price": 2000,
      "type": "plan",
      "is_active": 1
    },
    {
      "id": 3,
      "name": "Additional Storage",
      "price": 500,
      "type": "addon",
      "is_active": 1
    }
]
```

#### Get Item by ID

```
GET /items/1

Response:

```json
{
    "is_active": 1,
    "price": 1000,
    "name": "Basic Plan",
    "id": 1,
    "type": "plan"
}
```

#### Create Item

```
POST /items
```

Request Body:

```json
{
  "name": "Item 1",
  "price": 100,
  "type": "Type 1",
  "is_active": 1
}
```

#### Update Item

```
PUT /items/1
```

Request Body:

```json
{
  "name": "Item 1",
  "price": 100,
  "type": "Type 1",
  "is_active": 1
}
```

#### Deactivate Item

```
DELETE /items/{id}
```

### Subscriptions

#### Get All Subscriptions

```
GET /subscriptions
```

Response:

```json
[
  {
    "id": 1,
    "customer": 1,
    "billing_period": 1,
    "billing_period_unit": "month",
    "total_due": 100,
    "activated_at": "2022-01-01T00:00:00Z",
    "current_term_start": "2022-01-01T00:00:00Z",
    "current_term_end": "2022-12-31T23:59:59Z",
    "status": "active"
  },
  ...
]
```

#### Get Subscriptions Sorted by Current Term End

```
GET /subscriptions?sort_by=current_term_end&sort_type=desc
```

Response:

```json
[
  {
    "id": 1,
    "customer": 1,
    "billing_period": 1,
    "billing_period_unit": "month",
    "total_due": 100,
    "activated_at": "2022-01-01T00:00:00Z",
    "current_term_start": "2022-01-01T00:00:00Z",
    "current_term_end": "2022-12-31T23:59:59Z",
    "status": "active"
  },
  ...
]
```

#### Get Subscription by ID

```
GET /subscriptions/{id}
```

Response:

```json
{
  "id": 1,
  "customer": {
    "id": 1,
    "first_name": "John",
    "last_name": "Doe"
  },
  "billing_period": 1,
  "billing_period_unit": "month",
  "total_due": 100,
  "activated_at": "2022-01-01T00:00:00Z",
  "current_term_start": "2022-01-01T00:00:00Z",
  "current_term_end": "2022-12-31T23:59:59Z",
  "status": "active",
  "subscription_items": [
    {
      "quantity": 1,
      "amount": 100,
      "item": {
        "id": 1,
        "name": "Item 1",
        "price": 100,
        "type": "Type 1"
      }
    }
  ]
}
```

#### Create Subscription

```
POST /subscriptions
```

Request Body:

```json
{
  "customer_id": 1,
  "billing_period": 1,
  "billing_period_unit": "month",
  "total_due": 100,
  "activated_at": "2022-01-01T00:00:00Z",
  "current_term_start": "2022-01-01T00:00:00Z",
  "current_term_end": "2022-12-31T23:59:59Z",
  "status": "active",
  "shipping_address": {
    "title": "Home",
    "line1": "123 Main St",
    "line2": "",
    "city": "Anytown",
    "province": "CA",
    "postcode": "12345"
  },
  "card": {
    "card_type": "Visa",
    "masked_number": "4111********1111",
    "expiry_month": 12,
    "expiry_year": 2023,
    "status": "active",
    "is_primary": 1
  },
  "items": [
    {
      "item_id": 1,
      "quantity": 1,
      "price": 100,
      "amount": 100
    }
  ]
}
```
