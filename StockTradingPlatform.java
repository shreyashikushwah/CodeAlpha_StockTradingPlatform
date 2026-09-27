import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * CodeAlpha Java Internship - Task 2
 * Stock Trading Platform (Simulation)
 *
 * Features:
 *  - Simulated market with fluctuating stock prices
 *  - Buy / Sell operations
 *  - Portfolio tracking (holdings + cash + performance vs. starting balance)
 *  - Transaction history
 */
public class StockTradingPlatform {

    // ---------- Stock class ----------
    static class Stock {
        private String symbol;
        private String companyName;
        private double price;

        public Stock(String symbol, String companyName, double price) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.price = price;
        }

        public String getSymbol() { return symbol; }
        public String getCompanyName() { return companyName; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    // ---------- Transaction class ----------
    static class Transaction {
        String type;     // BUY or SELL
        String symbol;
        int quantity;
        double priceAtTime;

        public Transaction(String type, String symbol, int quantity, double priceAtTime) {
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.priceAtTime = priceAtTime;
        }

        @Override
        public String toString() {
            return String.format("%-4s %-6s qty:%-4d @ $%.2f", type, symbol, quantity, priceAtTime);
        }
    }

    // ---------- Market class (manages available stocks & price simulation) ----------
    static class Market {
        private ArrayList<Stock> stocks = new ArrayList<>();
        private Random random = new Random();

        public Market() {
            stocks.add(new Stock("AAPL", "Apple Inc.", 190.00));
            stocks.add(new Stock("GOOG", "Alphabet Inc.", 140.00));
            stocks.add(new Stock("TSLA", "Tesla Inc.", 250.00));
            stocks.add(new Stock("AMZN", "Amazon.com Inc.", 145.00));
            stocks.add(new Stock("MSFT", "Microsoft Corp.", 330.00));
        }

        public ArrayList<Stock> getStocks() { return stocks; }

        public Stock findStock(String symbol) {
            for (Stock s : stocks) {
                if (s.getSymbol().equalsIgnoreCase(symbol)) return s;
            }
            return null;
        }

        // Randomly fluctuates all stock prices by -5% to +5% to simulate a live market
        public void simulateMarketMovement() {
            for (Stock s : stocks) {
                double changePercent = (random.nextDouble() * 10) - 5; // -5 to +5
                double newPrice = s.getPrice() * (1 + changePercent / 100.0);
                s.setPrice(Math.round(newPrice * 100.0) / 100.0);
            }
        }

        public void displayMarket() {
            System.out.println("\n----- MARKET DATA -----");
            System.out.printf("%-6s %-20s %-10s%n", "Symbol", "Company", "Price");
            for (Stock s : stocks) {
                System.out.printf("%-6s %-20s $%-10.2f%n", s.getSymbol(), s.getCompanyName(), s.getPrice());
            }
        }
    }

    // ---------- User class (manages cash, holdings, transaction history) ----------
    static class User {
        private String name;
        private double cashBalance;
        private double initialInvestment;
        private Map<String, Integer> holdings = new HashMap<>(); // symbol -> quantity
        private ArrayList<Transaction> history = new ArrayList<>();

        public User(String name, double startingCash) {
            this.name = name;
            this.cashBalance = startingCash;
            this.initialInvestment = startingCash;
        }

        public boolean buy(Stock stock, int quantity) {
            double cost = stock.getPrice() * quantity;
            if (cost > cashBalance) {
                System.out.println("Insufficient funds. You have $" + String.format("%.2f", cashBalance));
                return false;
            }
            cashBalance -= cost;
            holdings.merge(stock.getSymbol(), quantity, Integer::sum);
            history.add(new Transaction("BUY", stock.getSymbol(), quantity, stock.getPrice()));
            return true;
        }

        public boolean sell(Stock stock, int quantity) {
            int owned = holdings.getOrDefault(stock.getSymbol(), 0);
            if (quantity > owned) {
                System.out.println("You only own " + owned + " shares of " + stock.getSymbol());
                return false;
            }
            cashBalance += stock.getPrice() * quantity;
            holdings.put(stock.getSymbol(), owned - quantity);
            history.add(new Transaction("SELL", stock.getSymbol(), quantity, stock.getPrice()));
            return true;
        }

        public void displayPortfolio(Market market) {
            System.out.println("\n----- PORTFOLIO: " + name + " -----");
            System.out.printf("Cash balance: $%.2f%n", cashBalance);

            double holdingsValue = 0;
            System.out.printf("%-6s %-10s %-12s %-12s%n", "Symbol", "Quantity", "Price", "Value");
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                if (entry.getValue() > 0) {
                    Stock s = market.findStock(entry.getKey());
                    double value = s.getPrice() * entry.getValue();
                    holdingsValue += value;
                    System.out.printf("%-6s %-10d $%-11.2f $%-11.2f%n",
                            entry.getKey(), entry.getValue(), s.getPrice(), value);
                }
            }

            double totalValue = cashBalance + holdingsValue;
            double profitLoss = totalValue - initialInvestment;
            System.out.printf("Total portfolio value: $%.2f%n", totalValue);
            System.out.printf("Overall Profit/Loss: %s$%.2f (%.2f%%)%n",
                    profitLoss >= 0 ? "+" : "-",
                    Math.abs(profitLoss),
                    (profitLoss / initialInvestment) * 100);
        }

        public void displayHistory() {
            System.out.println("\n----- TRANSACTION HISTORY -----");
            if (history.isEmpty()) {
                System.out.println("No transactions yet.");
                return;
            }
            for (Transaction t : history) {
                System.out.println(t);
            }
        }
    }

    // ---------- Main program ----------
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Market market = new Market();

        System.out.println("=====================================");
        System.out.println("      STOCK TRADING PLATFORM");
        System.out.println("=====================================");
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        User user = new User(name.isEmpty() ? "Trader" : name, 10000.00); // start with $10,000
        System.out.printf("Welcome, %s! You start with $%.2f virtual cash.%n", user.name, user.cashBalance);

        boolean running = true;
        while (running) {
            market.simulateMarketMovement(); // prices shift slightly each round, like a live market

            System.out.println("\n--- MENU ---");
            System.out.println("1. View market data");
            System.out.println("2. Buy stock");
            System.out.println("3. Sell stock");
            System.out.println("4. View portfolio");
            System.out.println("5. View transaction history");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1 -> market.displayMarket();
                case 2 -> tradeStock(scanner, market, user, true);
                case 3 -> tradeStock(scanner, market, user, false);
                case 4 -> user.displayPortfolio(market);
                case 5 -> user.displayHistory();
                case 6 -> {
                    System.out.println("Final portfolio summary:");
                    user.displayPortfolio(market);
                    System.out.println("Thanks for trading. Goodbye, " + user.name + "!");
                    running = false;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
        scanner.close();
    }

    private static void tradeStock(Scanner scanner, Market market, User user, boolean isBuy) {
        market.displayMarket();
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.findStock(symbol);

        if (stock == null) {
            System.out.println("Stock symbol not found.");
            return;
        }

        System.out.print("Enter quantity: ");
        int quantity = readInt(scanner);
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        boolean success = isBuy ? user.buy(stock, quantity) : user.sell(stock, quantity);
        if (success) {
            System.out.println((isBuy ? "Bought " : "Sold ") + quantity + " shares of " + symbol
                    + " at $" + String.format("%.2f", stock.getPrice()));
        }
    }

    private static int readInt(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume leftover newline
        return value;
    }
}
