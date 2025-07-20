import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Enum to represent transaction types
enum TransactionType {
    BUY,
    SELL
}

// Stock class represents a single stock in the market
class Stock {
    private String symbol;
    private String name;
    private double currentPrice;

    public Stock(String symbol, String name, double currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    // Method to simulate price fluctuation (optional, for a more dynamic market)
    // For this basic version, prices are static.
    // public void updatePrice(double newPrice) {
    //     this.currentPrice = newPrice;
    // }

    @Override
    public String toString() {
        return String.format("%-5s %-15s $%.2f", symbol, name, currentPrice);
    }
}

// Transaction class represents a buy or sell operation
class Transaction {
    private Stock stock;
    private int quantity;
    private double priceAtTransaction;
    private TransactionType type;
    private LocalDateTime timestamp;

    public Transaction(Stock stock, int quantity, double priceAtTransaction, TransactionType type) {
        this.stock = stock;
        this.quantity = quantity;
        this.priceAtTransaction = priceAtTransaction;
        this.type = type;
        this.timestamp = LocalDateTime.now(); // Record the time of the transaction
    }

    public Stock getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPriceAtTransaction() {
        return priceAtTransaction;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s %-4s %-5s %-15s Qty: %-5d @ $%.2f Total: $%.2f",
                timestamp.format(formatter),
                type,
                stock.getSymbol(),
                stock.getName(),
                quantity,
                priceAtTransaction,
                (quantity * priceAtTransaction));
    }
}

// Portfolio class manages a user's holdings and cash balance
class Portfolio {
    private double cashBalance;
    // Map to store stock holdings: Key = Stock Symbol, Value = Quantity
    private Map<String, Integer> holdings;
    private List<Transaction> transactionHistory;
    private double initialInvestment; // To track portfolio performance

    public Portfolio(double initialCash) {
        this.cashBalance = initialCash;
        this.holdings = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
        this.initialInvestment = initialCash; // Initial investment is the starting cash
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public Map<String, Integer> getHoldings() {
        return holdings;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    // Add cash to the portfolio
    public void addCash(double amount) {
        if (amount > 0) {
            cashBalance += amount;
            System.out.printf("Added $%.2f to cash balance. New balance: $%.2f%n", amount, cashBalance);
        } else {
            System.out.println("Amount to add must be positive.");
        }
    }

    // Deduct cash from the portfolio
    public boolean deductCash(double amount) {
        if (amount > 0 && cashBalance >= amount) {
            cashBalance -= amount;
            return true;
        }
        System.out.println("Insufficient cash balance or invalid amount.");
        return false;
    }

    // Add stock to holdings
    public void addHolding(String symbol, int quantity) {
        holdings.put(symbol, holdings.getOrDefault(symbol, 0) + quantity);
    }

    // Remove stock from holdings
    public boolean removeHolding(String symbol, int quantity) {
        if (holdings.containsKey(symbol) && holdings.get(symbol) >= quantity) {
            holdings.put(symbol, holdings.get(symbol) - quantity);
            if (holdings.get(symbol) == 0) {
                holdings.remove(symbol); // Remove entry if quantity becomes zero
            }
            return true;
        }
        System.out.println("You do not own enough shares of " + symbol + ".");
        return false;
    }

    // Record a transaction
    public void recordTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }

    // Get quantity of a specific stock held
    public int getHoldingQuantity(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    // Calculate the current total value of the portfolio (cash + market value of stocks)
    public double calculatePortfolioValue(StockMarket market) {
        double stockValue = 0;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String symbol = entry.getKey();
            int quantity = entry.getValue();
            Stock stock = market.getStock(symbol);
            if (stock != null) {
                stockValue += stock.getCurrentPrice() * quantity;
            }
        }
        return cashBalance + stockValue;
    }

    // Display current holdings
    public void displayHoldings(StockMarket market) {
        System.out.println("\n--- Your Holdings ---");
        if (holdings.isEmpty()) {
            System.out.println("You currently hold no stocks.");
        } else {
            System.out.printf("%-5s %-15s %-10s %-10s %-10s%n", "Symbol", "Name", "Quantity", "Price", "Value");
            System.out.println("-------------------------------------------------------");
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String symbol = entry.getKey();
                int quantity = entry.getValue();
                Stock stock = market.getStock(symbol);
                if (stock != null) {
                    double currentValue = stock.getCurrentPrice() * quantity;
                    System.out.printf("%-5s %-15s %-10d $%-9.2f $%-9.2f%n",
                            symbol, stock.getName(), quantity, stock.getCurrentPrice(), currentValue);
                }
            }
        }
        System.out.printf("Cash Balance: $%.2f%n", cashBalance);
        System.out.println("-------------------------------------------------------");
    }

    // Display transaction history
    public void displayTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions recorded yet.");
        } else {
            for (Transaction t : transactionHistory) {
                System.out.println(t);
            }
        }
        System.out.println("---------------------------");
    }

    // Calculate and display portfolio performance
    public void displayPerformance(StockMarket market) {
        double currentTotalValue = calculatePortfolioValue(market);
        double gainLoss = currentTotalValue - initialInvestment;
        double percentageGainLoss = (initialInvestment == 0) ? 0 : (gainLoss / initialInvestment) * 100;

        System.out.println("\n--- Portfolio Performance ---");
        System.out.printf("Initial Investment: $%.2f%n", initialInvestment);
        System.out.printf("Current Portfolio Value: $%.2f%n", currentTotalValue);
        System.out.printf("Gain/Loss: $%.2f%n", gainLoss);
        System.out.printf("Percentage Gain/Loss: %.2f%%%n", percentageGainLoss);
        System.out.println("-----------------------------");
    }
}

// User class (simplified for this simulation)
class User {
    private String username;
    private Portfolio portfolio;

    public User(String username, double initialCash) {
        this.username = username;
        this.portfolio = new Portfolio(initialCash);
    }

    public String getUsername() {
        return username;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}

// StockMarket class simulates available stocks and their prices
class StockMarket {
    private Map<String, Stock> availableStocks;

    public StockMarket() {
        availableStocks = new HashMap<>();
        initializeStocks();
    }

    // Initialize with some sample stocks
    private void initializeStocks() {
        availableStocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 175.00));
        availableStocks.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 420.00));
        availableStocks.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 150.00));
        availableStocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 180.00));
        availableStocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 185.00));
    }

    // Get a stock by its symbol
    public Stock getStock(String symbol) {
        return availableStocks.get(symbol.toUpperCase());
    }

    // Display all available stocks in the market
    public void displayMarketData() {
        System.out.println("\n--- Current Market Data ---");
        System.out.printf("%-5s %-15s %-10s%n", "Symbol", "Company Name", "Price");
        System.out.println("--------------------------------");
        for (Stock stock : availableStocks.values()) {
            System.out.println(stock);
        }
        System.out.println("--------------------------------");
    }
}

// Main class for the Stock Trading Platform simulation
public class TradingPlatform {
    private User currentUser;
    private StockMarket market;
    private Scanner scanner;

    public TradingPlatform() {
        // Initialize market and a default user with some starting cash
        this.market = new StockMarket();
        this.currentUser = new User("Trader123", 10000.00); // User starts with $10,000 cash
        this.scanner = new Scanner(System.in);
    }

    // Display the main menu options
    private void displayMainMenu() {
        System.out.println("\n--- Stock Trading Platform ---");
        System.out.println("Welcome, " + currentUser.getUsername() + "!");
        System.out.printf("Current Cash Balance: $%.2f%n", currentUser.getPortfolio().getCashBalance());
        System.out.println("1. View Market Data");
        System.out.println("2. Buy Stock");
        System.out.println("3. Sell Stock");
        System.out.println("4. View Portfolio Holdings");
        System.out.println("5. View Transaction History");
        System.out.println("6. View Portfolio Performance");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    // Handle user's menu choice
    private void executeChoice(int choice) {
        switch (choice) {
            case 1:
                market.displayMarketData();
                break;
            case 2:
                buyStock();
                break;
            case 3:
                sellStock();
                break;
            case 4:
                currentUser.getPortfolio().displayHoldings(market);
                break;
            case 5:
                currentUser.getPortfolio().displayTransactionHistory();
                break;
            case 6:
                currentUser.getPortfolio().displayPerformance(market);
                break;
            case 0:
                System.out.println("Exiting Stock Trading Platform. Happy Trading!");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // Logic for buying stock
    private void buyStock() {
        System.out.print("Enter stock symbol to buy (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stockToBuy = market.getStock(symbol);

        if (stockToBuy == null) {
            System.out.println("Stock not found. Please enter a valid symbol.");
            return;
        }

        System.out.print("Enter quantity to buy: ");
        int quantity = -1;
        try {
            quantity = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid quantity. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
            return;
        }
        scanner.nextLine(); // Consume newline

        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        double totalCost = stockToBuy.getCurrentPrice() * quantity;
        Portfolio portfolio = currentUser.getPortfolio();

        if (portfolio.getCashBalance() >= totalCost) {
            if (portfolio.deductCash(totalCost)) {
                portfolio.addHolding(symbol, quantity);
                portfolio.recordTransaction(new Transaction(stockToBuy, quantity, stockToBuy.getCurrentPrice(), TransactionType.BUY));
                System.out.printf("Successfully bought %d shares of %s for $%.2f.%n", quantity, symbol, totalCost);
            }
        } else {
            System.out.printf("Insufficient funds. You need $%.2f but have $%.2f.%n", totalCost, portfolio.getCashBalance());
        }
    }

    // Logic for selling stock
    private void sellStock() {
        System.out.print("Enter stock symbol to sell (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stockToSell = market.getStock(symbol);

        if (stockToSell == null) {
            System.out.println("Stock not found. Please enter a valid symbol.");
            return;
        }

        System.out.print("Enter quantity to sell: ");
        int quantity = -1;
        try {
            quantity = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid quantity. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
            return;
        }
        scanner.nextLine(); // Consume newline

        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        Portfolio portfolio = currentUser.getPortfolio();
        int availableQuantity = portfolio.getHoldingQuantity(symbol);

        if (availableQuantity >= quantity) {
            double totalRevenue = stockToSell.getCurrentPrice() * quantity;
            if (portfolio.removeHolding(symbol, quantity)) {
                portfolio.addCash(totalRevenue); // Add revenue from sale to cash balance
                portfolio.recordTransaction(new Transaction(stockToSell, quantity, stockToSell.getCurrentPrice(), TransactionType.SELL));
                System.out.printf("Successfully sold %d shares of %s for $%.2f.%n", quantity, symbol, totalRevenue);
            }
        } else {
            System.out.printf("You only have %d shares of %s. Cannot sell %d shares.%n", availableQuantity, symbol, quantity);
        }
    }

    // Main application loop
    public void run() {
        int choice = -1;
        while (choice != 0) {
            displayMainMenu();
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                executeChoice(choice);
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number corresponding to the menu option.");
                scanner.nextLine(); // Consume the invalid input
                choice = -1; // Reset choice to keep loop running
            }
        }
        scanner.close(); // Close the scanner when done
    }

    public static void main(String[] args) {
        TradingPlatform platform = new TradingPlatform();
        platform.run();
    }
}
