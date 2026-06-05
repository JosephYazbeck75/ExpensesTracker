package desktopapps.oopfinalproject;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.scene.Parent;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

import javafx.scene.control.Alert.AlertType;

public class ExpensesTracker extends Application {
    public static class Expense {
        private String description;
        private double amount;
        private LocalDate date;
        private String category;

        public Expense(String description, double amount, LocalDate date, String category) {
            this.description = description;
            this.amount = amount;
            this.date = date;
            this.category = category;
        }

        public String getDescription() {
            return description;
        }
        public double getAmount() {
            return amount;
        }
        public LocalDate getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }


        public void setDescription(String description) {
            this.description = description;
        }
        public void setAmount(double amount) {
            this.amount = amount;
        }
        public void setDate(LocalDate date) {
            this.date = date;
        }
        public void setCategory(String category) {
            this.category = category;
        }
    }

    private HBox chartBox;
    private TableView<Expense> tableView;
    private TextField descField, amountField;
    private DatePicker datePicker;
    private ComboBox<String> categoryBox;
    private Label totalLabel;
    private PieChart pieChart;
    private BarChart<String, Number> histogram;
    private Label messageLabel;
    private Alert alert = new Alert(AlertType.WARNING);
    private double totalExpenses = 0;
    private boolean darkMode = false;
    private Map<String, String> userDatabase = new HashMap<>();
    private Map<String, List<Expense>> userExpenses = new HashMap<>();
    private Double monthlyIncome = null;
    private VBox legendBox = new VBox();
    private VBox histogramLegendBox = new VBox();
    private Scene currentScene;
    private boolean darkModeEnabled = false;
    private String currentUser;

    TextField newCategoryField = new TextField();
    Button addCategoryButton = new Button("Add Category");
    ComboBox<String> categoryComboBox = new ComboBox<>();
    private final Map<String, Color> categoryColors = new HashMap<>(Map.of(
            "Food", Color.web("#e74c3c"),
            "Transport", Color.web("#3498db"),
            "Entertainment", Color.web("#9b59b6"),
            "Utilities", Color.web("#f1c40f"),
            "Other", Color.web("#2ecc71"),
            "Remaining", Color.web("#95a5a6")
    ));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        showLoginScreen(stage);
    }

    private void showLoginScreen(Stage stage) {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Login");

        Button toSignUpButton = new Button("Sign Up");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> showLoginScreen(new Stage()));
        HBox logoutBox = new HBox(logoutButton);
        logoutBox.setAlignment(Pos.TOP_LEFT);
        logoutBox.setPadding(new Insets(10));

        Label loginMessage = new Label();

        VBox loginBox = new VBox(10, userLabel, userField, passLabel, passField, loginButton, toSignUpButton, loginMessage);
        loginBox.setPadding(new Insets(20));
        loginBox.setAlignment(Pos.CENTER);
        loginButton.setStyle(buttonStyle());
        toSignUpButton.setStyle(buttonStyle());

        loginButton.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if (userDatabase.containsKey(user) && userDatabase.get(user).equals(pass)) {
                currentUser = user;
                if (!userExpenses.containsKey(user)) {
                    userExpenses.put(user, new ArrayList<>());
                }
                showExpensesScreen(stage, user);
            } else {
                loginMessage.setText("Invalid credentials.");
                loginMessage.setStyle("-fx-text-fill: red;");
            }
        });

        toSignUpButton.setOnAction(e -> showSignUpScreen(stage));

        Scene scene = new Scene(loginBox, 300, 280);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }
    private Map<String, ColorPicker> categoryColorPickers = new HashMap<>();

    private void createCategoryColorPickers() {
        for (String category : categoryColors.keySet()) {
            ColorPicker colorPicker = new ColorPicker(categoryColors.get(category));
            colorPicker.setOnAction(e -> updateCategoryColor(category, colorPicker.getValue()));
            categoryColorPickers.put(category, colorPicker);
        }
    }

    private VBox createCategoryColorPickerBox() {
        VBox colorPickerBox = new VBox(10);
        categoryColorPickers.forEach((category, colorPicker) -> {
            HBox colorPickerRow = new HBox(10, new Label(category), colorPicker);
            colorPickerBox.getChildren().add(colorPickerRow);
        });
        return colorPickerBox;
    }

    private void showSignUpScreen(Stage stage) {
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button signUpButton = new Button("Register");
        Label signupMessage = new Label();

        VBox signUpBox = new VBox(10, userLabel, userField, passLabel, passField, signUpButton, signupMessage);
        signUpBox.setPadding(new Insets(20));
        signUpBox.setAlignment(Pos.CENTER);

        signUpButton.setStyle(buttonStyle());
        signUpButton.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();
            if (user.isEmpty() || pass.isEmpty()) {
                signupMessage.setText("Please fill in all fields.");
            } else if (userDatabase.containsKey(user)) {
                signupMessage.setText("Username already exists.");
            } else {
                userDatabase.put(user, pass);
                userExpenses.put(user, new ArrayList<>());
                showLoginScreen(stage);
            }
        });

        Scene scene = new Scene(signUpBox, 300, 250);
        stage.setTitle("Sign Up");
        stage.setScene(scene);
        stage.show();
    }

    private void showExpensesScreen(Stage stage, String username) {
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> {
            stage.close();
            showLoginScreen(new Stage());
        });

        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        welcomeLabel.setAlignment(Pos.CENTER);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, logoutButton, leftSpacer, welcomeLabel, rightSpacer);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.setStyle("-fx-background-color: #f0f0f0;");
        if (monthlyIncome == null) {
            TextInputDialog incomeDialog = new TextInputDialog();
            incomeDialog.setHeaderText("Enter Monthly Income");
            incomeDialog.setContentText("Monthly Income:");
            incomeDialog.showAndWait().ifPresent(input -> {
                try {
                    monthlyIncome = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    monthlyIncome = 0.0;
                }
            });
        }

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Expense, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        tableView.getColumns().addAll(descCol, amountCol, dateCol, categoryCol);

        descField = new TextField();
        descField.setPromptText("Description");
        descField.setPrefWidth(150);

        amountField = new TextField();
        amountField.setPromptText("Amount");

        datePicker = new DatePicker(LocalDate.now());
        categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Transport", "Entertainment", "Utilities", "Other");
        categoryBox.setValue("Other");

        Button addButton = new Button("Add Expense");

        Button deleteButton = new Button("Delete Selected");

        Button modifyButton = new Button("Modify Selected");
        modifyButton.setStyle(buttonStyle());

        Button exportCSV = new Button("Export CSV");

        Button saveGraph = new Button("Save Charts");

        Button toggleDarkMode = new Button("Toggle Dark Mode");
        toggleDarkMode.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold;");

        Button modifyExpense = new Button("Modify Selected");
        modifyButton.setStyle(buttonStyle());
        modifyButton.setOnAction(e -> modifySelectedExpense(username));

        List<Button> buttons = List.of(addButton, deleteButton, exportCSV, saveGraph);
        buttons.forEach(b -> b.setStyle(buttonStyle()));
        modifyButton.setOnAction(e -> modifySelectedExpense(username));

        totalLabel = new Label("Total: $0.00");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: green;");

        HBox inputFieldsBox = new HBox(10, descField, amountField, datePicker, categoryBox);
        HBox buttonsBox = new HBox(10, addButton, modifyButton, deleteButton, exportCSV, saveGraph, toggleDarkMode);

        logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> {
            stage.close();
            showLoginScreen(new Stage());
        });

        HBox logoutBox = new HBox(logoutButton);
        logoutBox.setAlignment(Pos.TOP_LEFT);
        logoutBox.setPadding(new Insets(10));

        VBox inputBox = new VBox(10, inputFieldsBox, buttonsBox);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);

        pieChart = new PieChart();
        pieChart.setLegendVisible(false);
        histogram = new BarChart<>(new CategoryAxis(), new NumberAxis());
        histogram.setTitle("Expenses by Category");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        VBox pieChartBox = new VBox(5, pieChart, legendBox);
        pieChartBox.setAlignment(Pos.CENTER);

        VBox histogramBox = new VBox(5, histogram, histogramLegendBox);
        histogramBox.setAlignment(Pos.CENTER);

        chartBox = new HBox(20, pieChartBox, histogramBox);
        chartBox.setPadding(new Insets(10));
        chartBox.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(10, topBar, inputBox, tableView, totalLabel, chartBox, messageLabel);
        mainLayout.setPadding(new Insets(10));

        addButton.setOnAction(e -> addExpense(username));
        deleteButton.setOnAction(e -> deleteSelectedExpense(username));
        exportCSV.setOnAction(e -> exportToCSV());
        saveGraph.setOnAction(e -> saveChartsAsImage());
        toggleDarkMode.setOnAction(e -> {
            darkModeEnabled = !darkModeEnabled;
            applyTheme(currentScene, darkModeEnabled);
        });

        refreshTable(username);
        updateCharts();

        currentScene = new Scene(mainLayout, 950, 700);
        stage.setTitle("Expenses Tracker");
        stage.setScene(currentScene);
        stage.show();
    }

    private void addExpense(String username) {
        try {
            String desc = descField.getText();
            if (desc.isEmpty()) {
                showWarning("Description cannot be empty.");
                return;
            }
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = datePicker.getValue();
            String category = categoryBox.getValue();

            double currentTotal = userExpenses.get(username).stream().mapToDouble(Expense::getAmount).sum();
            if (monthlyIncome != null && (currentTotal + amount) > monthlyIncome) {
                alert.setTitle("Warning!");
                alert.setHeaderText("An error has occured!");
                alert.setContentText("Amount exceeds monthly income");
                alert.showAndWait();
                return;
            }

            Expense expense = new Expense(desc, amount, date, category);
            userExpenses.get(username).add(expense);
            refreshTable(username);
            updateCharts();
            descField.clear();
            amountField.clear();
            messageLabel.setText("");
        } catch (NumberFormatException e) {
            alert.setTitle("Warning!");
            alert.setHeaderText("An error has occured!");
            alert.setContentText("Set a valid amount");
            alert.showAndWait();
        }
    }

    private void deleteSelectedExpense(String username) {
        Expense selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            userExpenses.get(username).remove(selected);
            refreshTable(username);
            updateCharts();
        }
    }

    private void modifySelectedExpense(String username) {
        Expense selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String desc = descField.getText();
                if (desc.isEmpty()) {
                    showWarning("Description cannot be empty.");
                    return;
                }
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = datePicker.getValue();
                String category = categoryBox.getValue();

                double currentTotal = userExpenses.get(username).stream()
                        .filter(e -> e != selected)
                        .mapToDouble(Expense::getAmount)
                        .sum();
                 if (monthlyIncome != null && monthlyIncome > 0 && (currentTotal + amount) > monthlyIncome) {
                        showWarning("Modified amount exceeds monthly income!");
                        return;
                 }

                    selected.setDescription(desc);
                    selected.setAmount(amount);
                    selected.setDate(date);
                    selected.setCategory(category);

                    refreshTable(username);
                    updateCharts();
                    messageLabel.setText("");
                } catch (NumberFormatException e) {
                alert.setTitle("Warning!");
                alert.setHeaderText("An error has occured!");
                alert.setContentText("Set a valid amount");
                alert.showAndWait();
                }
            }
        }

    private void refreshTable(String username) {
        tableView.getItems().setAll(userExpenses.get(username));

        totalExpenses = userExpenses.get(username).stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double remainingBudget = (monthlyIncome != null) ? Math.max(0, monthlyIncome - totalExpenses) : 0;

        totalLabel.setText("Monthly Budget: $" + String.format("%.2f", monthlyIncome != null ? monthlyIncome : 0.0)
                + " | Remaining: $" + String.format("%.2f", remainingBudget));
    }

    private void updateCharts() {
        pieChart.getData().clear();
        histogram.getData().clear();
        legendBox.getChildren().clear();
        histogramLegendBox.getChildren().clear();

        Map<String, Double> categoryTotals = new HashMap<>();
            for (Expense e : tableView.getItems()) {
                categoryTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
        }

        double remaining = monthlyIncome != null ? Math.max(0, monthlyIncome - totalExpenses) : 0;
        categoryTotals.put("Rest", remaining);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String category = entry.getKey();
            double value = entry.getValue();
            Color color = categoryColors.getOrDefault(category, Color.GRAY);

            PieChart.Data slice = new PieChart.Data(category, value);
            pieChart.getData().add(slice);

            Platform.runLater(() -> {
                Node pieNode = slice.getNode();
                if (pieNode != null) {
                    pieNode.setStyle("-fx-pie-color: " + toRgbString(color) + ";");
                }
            });

            XYChart.Data<String, Number> barData = new XYChart.Data<>(category, value);
            series.getData().add(barData);

            Node pieNode = slice.getNode();
            if (pieNode != null) {
                pieNode.setStyle("-fx-pie-color: " + toRgbString(color) + ";");
            }

            Node barNode = barData.getNode();
            if (barNode != null) {
                barNode.setStyle("-fx-bar-fill: " + toRgbString(color) + ";");
            }

            Rectangle legendRect = new Rectangle(10, 10, color);
            legendBox.getChildren().add(new HBox(5, legendRect, new Label(category)));

            Rectangle barLegend = new Rectangle(10, 10, color);
            histogramLegendBox.getChildren().add(new HBox(5, barLegend, new Label(category)));
        }

        histogram.getData().add(series);
    }

    private void updateCategoryColor(String category, Color newColor) {
        categoryColors.put(category, newColor);
        updateCharts();
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Table as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Description,Amount,Date,Category\n");

                double total = 0.0;
                for (Expense expense : tableView.getItems()) {
                    writer.write(String.format("%s,%.2f,%s,%s\n",
                            expense.getDescription(),
                            expense.getAmount(),
                            expense.getDate(),
                            expense.getCategory()));
                    total += expense.getAmount();
                }

                writer.write("\nTOTAL,");
                writer.write(String.format("%.2f\n", total));

                messageLabel.setText("File saved successfully.");
                messageLabel.setStyle("-fx-text-fill: green;");

            } catch (IOException e) {
                alert.setTitle("Warning!");
                alert.setHeaderText("An error has occured!");
                alert.setContentText("Error saving file");
                alert.showAndWait();
            }
        }
    }

    private void saveChartsAsImage() {
        if (chartBox == null) return;

        WritableImage fxImage = chartBox.snapshot(new SnapshotParameters(), null);

        BufferedImage bImage = new BufferedImage((int) fxImage.getWidth(), (int) fxImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = fxImage.getPixelReader();
        for (int y = 0; y < fxImage.getHeight(); y++) {
            for (int x = 0; x < fxImage.getWidth(); x++) {
                javafx.scene.paint.Color fxColor = reader.getColor(x, y);
                int argb = ((int) (fxColor.getOpacity() * 255) << 24) |
                        ((int) (fxColor.getRed() * 255) << 16) |
                        ((int) (fxColor.getGreen() * 255) << 8) |
                        ((int) (fxColor.getBlue() * 255));
                bImage.setRGB(x, y, argb);
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                ImageIO.write(bImage, "png", file);
                messageLabel.setText("Chart image saved successfully.");
                messageLabel.setStyle("-fx-text-fill: green;");
            } catch (IOException e) {
                alert.setTitle("Warning!");
                alert.setHeaderText("An error has occured!");
                alert.setContentText("Error saving char image");
            }
        }
    }

    private String toRgbString(Color c) {
        return String.format("rgb(%d, %d, %d)", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private String buttonStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 14px;"
                + "-fx-padding: 6px 12px;"
                + "-fx-background-radius: 5;";
    }
    private void applyTheme(Scene scene, boolean dark) {
        if (dark) {
            scene.getRoot().setStyle("-fx-base: #2b2b2b; -fx-background-color: #2b2b2b; -fx-text-fill: white;");
            applyTextFill(scene.getRoot(), "white");
        } else {
            scene.getRoot().setStyle("");
            applyTextFill(scene.getRoot(), "black");
        }
    }
    private void applyTextFill(Parent root, String color) {
        for (Node node : root.lookupAll("*")) {
            if (node instanceof Labeled) {
                ((Labeled) node).setTextFill(Color.web(color));
            } else if (node instanceof Text) {
                ((Text) node).setFill(Color.web(color));
            }
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void addNewCategory() {
        String newCategory = newCategoryField.getText().trim();

        if (newCategory.isEmpty()) {
            alert.setTitle("Warning!");
            alert.setHeaderText("An error has occured!");
            alert.setContentText("Category can not be empty!");
            return;
        }

        if (categoryBox.getItems().contains(newCategory)) {
            alert.setTitle("Warning!");
            alert.setHeaderText("An error has occured!");
            alert.setContentText("Category already exists!");
            return;
        }

        if (newCategory.length() > 20) {
            messageLabel.setText("Category name is too long (max 20 characters).");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        categoryBox.getItems().add(newCategory);

        Color newCategoryColor = Color.web(generateRandomColor());

        categoryColors.put(newCategory, newCategoryColor);

        newCategoryField.clear();
        updateCharts();

        messageLabel.setText("");
    }

    private String generateRandomColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}