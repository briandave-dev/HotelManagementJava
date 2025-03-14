# Hotel Management System

A comprehensive Java application for managing hotel operations including client management, room bookings, reservations, and invoice generation.

## Features

- **Client Management**: Add, edit, delete, and search for client records
- **Room Management**: Manage different room types, rates, and availability
- **Reservation System**: Create and manage room reservations with date tracking
- **Invoice Generation**: Automatically generate detailed invoices with PDF export
- **User-Friendly Interface**: Modern Swing GUI with intuitive navigation

## Technical Details

- **Language**: Java
- **UI Framework**: Swing
- **Database**: SQL (with JDBC)
- **PDF Generation**: iText library
- **Architecture**: MVC pattern

## Screenshots

*[Screenshots would be placed here]*

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- MySQL database

### Quick Start

The easiest way to run the application is using the provided batch file:

1. Ensure you have Java installed
2. Configure your database connection in the `.env` file
3. Double-click the `run.bat` file (Windows) or execute `./run.sh` (Linux/Mac)

### Manual Compilation and Execution

If you prefer to compile and run the application manually:

1. Compile the Java files:
   ```
   javac -cp "lib/*:." -d out $(find src/main/java -name "*.java")
   ```

2. Run the application:
   ```
   java -cp "out:lib/*" com.hotel.Main
   ```

Note: On Windows, use semicolons instead of colons in the classpath:
```
javac -cp "lib/*;." -d out $(find src/main/java -name "*.java")
java -cp "out;lib/*" com.hotel.Main
```

### Manual Installation with Maven

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/hotel-management-system.git
   ```

2. Navigate to the project directory:
   ```
   cd hotel-management-system
   ```

3. Build the project:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   java -jar HotelApp.jar
   ```

## Database Configuration

Create a `.env` file in the project root with the following content:
```
DB_URL=jdbc:mysql://localhost:3306/hotel_db
DB_USER=your_username
DB_PASSWORD=your_password
```

## Usage

1. **Managing Clients**:
   - Navigate to the Clients menu
   - Use the form to add new clients or edit existing ones
   - View client history and details

2. **Managing Rooms**:
   - Use the Rooms menu to add or modify room information
   - Set room categories, rates, and amenities

3. **Creating Reservations**:
   - Select a client and available room
   - Set check-in and check-out dates
   - The system will calculate the total cost automatically

4. **Generating Invoices**:
   - Invoices are automatically created for reservations
   - Export invoices as PDF documents for printing or sharing

## Project Structure

- `src/main/java/com/hotel/model/` - Data models
- `src/main/java/com/hotel/view/` - UI components
- `src/main/java/com/hotel/service/` - Business logic
- `src/main/java/com/hotel/util/` - Utility classes

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- iText for PDF generation capabilities
- All contributors who have helped improve this system