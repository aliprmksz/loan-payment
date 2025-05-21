# loan-payment


## Features
- **Create Loan**: Create a new loan with specified amount, interest rate, and number of installments.
- **List Loans**: Retrieve loans for a customer, with optional filters for number of installments and paid status.
- **List Installments**: View all installments for a specific loan.
- **Pay Loan**: Pay one or more installments with reward/penalty adjustments based on payment timing.
- **Role-Based Security**: Supports `ADMIN` (full access) and `CUSTOMER` (self-only access) roles with JWT authentication.

## Prerequisites
- Java 21
- Maven
- H2 Database (in-memory, included)

## Setup and Running

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/aliprmksz/loan-payment.git
   cd loan-payment
   
2. **Build the Project**:
   
3. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

The requests:

Create Customer

curl --location 'http://localhost:8080/api/auth/create' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=2082E1BE405EB36AB31F6C01071079AF' \
--data '{
"name": "test",
"surname": "test_user_1",
"creditLimit": 10000,
"usedCreditLimit": 0,
"role": "CUSTOMER",
"username": "test_1",
"password": "password"
}'

Generate Token
curl --location 'http://localhost:8080/api/auth/login' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=2082E1BE405EB36AB31F6C01071079AF' \
--data '{
"username": "test_1",
"password": "password"
}'


Create Loan
curl --location 'http://localhost:8080/api/loans' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <token>' \
--header 'Cookie: JSESSIONID=2082E1BE405EB36AB31F6C01071079AF' \
--data '{
"customerId": "1",
"amount": 1000,
"interestRate": 0.2,
"numberOfInstallments": 12
}'


Get Loan List of Customer
curl --location 'http://localhost:8080/api/loans/customer/1' \
--header 'Authorization: Bearer <token>' \
--header 'Cookie: JSESSIONID=2082E1BE405EB36AB31F6C01071079AF'


Get List of Loan Installments
curl --location 'http://localhost:8080/api/loans/1/installments' \
--header 'Authorization: Bearer token' \
--header 'Cookie: JSESSIONID=2082E1BE405EB36AB31F6C01071079AF'


Pay Loan
curl --location 'http://localhost:8080/api/loans/pay' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <token>' \
--header 'Cookie: JSESSIONID=2082E1BE405EB36AB31F6C01071079AF' \
--data '{
"loanId": 2,
"amount": 300
}'
