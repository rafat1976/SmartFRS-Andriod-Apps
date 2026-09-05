from flask import Flask, render_template, request, redirect, url_for, session, flash, jsonify
import pyodbc
import random
from datetime import datetime

app = Flask(__name__)
app.secret_key = 'flight_super_secret_key'

# ========================================================
# DATABASE CONNECTION
# ========================================================
def get_db_connection():
    return pyodbc.connect(
        r'Driver={SQL Server};'
        r'Server=.;'
        r'Database=FRS;'
        r'Trusted_Connection=yes;'
    )

# ========================================================
# ROUTES & LOGIC
# ========================================================
@app.route('/')
def index():
    return render_template('index.html')

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        if request.is_json or request.headers.get('Content-Type') == 'application/json':
            data = request.get_json(silent=True) or {}
            email = data.get('email')
            password = data.get('password')
            name = data.get('name', '')
            try:
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("SELECT * FROM Users WHERE Username = ?", (email,))
                if cursor.fetchone():
                    conn.close()
                    return jsonify({'status': False, 'message': 'This email is already registered!'})
                cursor.execute("INSERT INTO Users (Username, Password, Role) VALUES (?, ?, 'User')", (email, password))
                conn.commit()
                conn.close()
                return jsonify({'status': True, 'message': 'Registration Successful!'})
            except Exception as e:
                return jsonify({'status': False, 'message': f'Registration error: {e}'}), 500

        email = request.form['email']
        password = request.form['password']
        selected_role = request.form.get('role')
        
        if selected_role == 'Admin':
            flash("Registration as Admin is not allowed! Please register as User.")
            return redirect(url_for('register'))
            
        try:
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM Users WHERE Username = ?", (email,))
            if cursor.fetchone():
                conn.close()
                flash("This email is already registered! Please login.")
                return redirect(url_for('register'))
                
            cursor.execute("INSERT INTO Users (Username, Password, Role) VALUES (?, ?, 'User')", (email, password))
            conn.commit()
            conn.close()
            flash("Registration Successful! Please login.")
            return redirect(url_for('login'))
        except Exception as e:
            flash(f"An error occurred during registration: {e}")
            return redirect(url_for('register'))
            
    return render_template('register.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        if request.is_json or request.headers.get('Content-Type') == 'application/json':
            data = request.get_json(silent=True) or {}
            email = data.get('email')
            password = data.get('password')
            
            if email == 'Admin' and password == 'Admin':
                return jsonify({'status': True, 'message': 'Admin Login Successful', 'name': 'Admin'})
                
            try:
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("SELECT Username, Role FROM Users WHERE Username=? AND Password=?", (email, password))
                user = cursor.fetchone()
                conn.close()
                
                if user:
                    return jsonify({'status': True, 'message': 'Login Successful', 'name': user[0]})
                else:
                    return jsonify({'status': False, 'message': 'Invalid Email or Password', 'name': None})
            except Exception as e:
                return jsonify({'status': False, 'message': f"Database error: {str(e)}", 'name': None}), 500

        email = request.form['email']
        password = request.form['password']
        selected_role = request.form.get('role')
        
        if email == 'Admin' and password == 'Admin' and selected_role == 'Admin':
            session['user'] = 'Master_Admin'
            session['role'] = 'Admin'
            return redirect(url_for('admin_dashboard'))
            
        try:
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("SELECT Username, Role FROM Users WHERE Username=? AND Password=? AND Role=?",
                           (email, password, selected_role))
            user = cursor.fetchone()
            conn.close()
            
            if user:
                session['user'] = user[0]
                session['role'] = user[1]
                if session['role'] == 'Admin':
                    return redirect(url_for('admin_dashboard'))
                else:
                    return redirect(url_for('index'))
            else:
                flash("Invalid Email, Password, or Role! Please try again.")
                return redirect(url_for('login'))
        except Exception as e:
            flash(f"Login error: {e}")
            return redirect(url_for('login'))
            
    return render_template('login.html')

@app.route('/logout')
def logout():
    session.clear()
    flash("You have been logged out.")
    return redirect(url_for('login'))

@app.route('/search_ui')
def search_ui():
    places = []
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT DISTINCT Source FROM Flight UNION SELECT DISTINCT Destination FROM Flight")
        rows = cursor.fetchall()
        places = [r[0] for r in rows if r[0]]
        if not places:
            cursor.execute("SELECT PlaceName FROM Places")
            rows = cursor.fetchall()
            places = [r[0] for r in rows if r[0]]
        conn.close()
    except Exception as e:
        print("Error fetching places:", e)
        places = ["Dhaka", "Chittagong", "Cox's Bazar", "Sylhet", "Rajshahi", "Saidpur"]
    return render_template('search_ui.html', places=places)

@app.route('/search', methods=['POST'])
def search():
    source = request.form.get('source')
    destination = request.form.get('destination')
    flights = []
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT FlightId, FlightName, Source, Destination, Dep_time, Arr_time, Price FROM Flight WHERE Source=? AND Destination=?", (source, destination))
        rows = cursor.fetchall()
        for r in rows:
            flights.append({
                'FlightId': r[0],
                'FlightName': r[1],
                'Source': r[2],
                'Destination': r[3],
                'Dep_Time': r[4] if len(r) > 4 else '10:00 AM',
                'Arr_Time': r[5] if len(r) > 5 else '11:30 AM',
                'Price': r[6] if len(r) > 6 else '5000'
            })
        conn.close()
    except Exception as e:
        print("Search error:", e)
    return render_template('search.html', flights=flights)

@app.route('/passenger_details/<flight_name>')
def passenger_details(flight_name):
    return render_template('passenger_details.html', flight=flight_name)

@app.route('/payment', methods=['POST'])
def payment():
    flight_name = request.form.get('flight_name')
    fname = request.form.get('fname')
    lname = request.form.get('lname')
    email = request.form.get('email')
    mobile = request.form.get('mobile')
    nid = request.form.get('nid')
    dob = request.form.get('dob')
    address = request.form.get('address')
    p_class = request.form.get('p_class')
    gender = request.form.get('gender')
    
    p_data = {
        'fname': fname,
        'lname': lname,
        'email': email,
        'mobile': mobile,
        'nid': nid,
        'dob': dob,
        'address': address,
        'p_class': p_class,
        'gender': gender
    }
    return render_template('payment.html', flight=flight_name, p_data=p_data)

@app.route('/confirm_booking', methods=['POST'])
def confirm_booking():
    flight = request.form.get('flight')
    nid = request.form.get('nid')
    fname = request.form.get('fname', '')
    lname = request.form.get('lname', '')
    
    ticket_no = f"TKT{random.randint(10000, 99999)}"
    username = session.get('user', fname if fname else 'Guest')
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("INSERT INTO Bookings (TicketNo, Username, FlightName, SeatNo) VALUES (?, ?, ?, ?)",
                       (ticket_no, username, flight, nid if nid else 'NID-N/A'))
        conn.commit()
        conn.close()
    except Exception as e:
        print("Confirm booking DB error:", e)
        
    return render_template('booking.html', ticket_no=ticket_no, flight=flight, nid=nid)

@app.route('/cancel_ticket', methods=['GET', 'POST'])
def cancel_ticket():
    if request.method == 'POST':
        ticket_no = request.form.get('ticket_no')
        try:
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("DELETE FROM Bookings WHERE TicketNo=?", (ticket_no,))
            conn.commit()
            conn.close()
        except Exception as e:
            print("Cancel ticket error:", e)
        return render_template('cancel_success.html', ticket_no=ticket_no)
    return render_template('cancel.html')

@app.route('/admin_dashboard')
def admin_dashboard():
    if session.get('role') != 'Admin':
        flash("Admin access required!")
        return redirect(url_for('login'))
        
    flights = []
    bookings = []
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT FlightId, FlightName, Source, Destination, Price FROM Flight")
        flights = cursor.fetchall()
        cursor.execute("SELECT TicketNo, Username, FlightName, SeatNo FROM Bookings")
        bookings = cursor.fetchall()
        conn.close()
    except Exception as e:
        print("Admin dashboard error:", e)
        
    return render_template('admin_dashboard.html', flights=flights, bookings=bookings)

@app.route('/add_flight', methods=['POST'])
def add_flight():
    if session.get('role') != 'Admin':
        return redirect(url_for('login'))
        
    name = request.form.get('name')
    source = request.form.get('source')
    dest = request.form.get('dest')
    price = request.form.get('price')
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("INSERT INTO Flight (FlightName, Source, Destination, Price) VALUES (?, ?, ?, ?)",
                       (name, source, dest, price))
        conn.commit()
        conn.close()
        flash("New flight added successfully!")
    except Exception as e:
        flash(f"Error adding flight: {e}")
        
    return redirect(url_for('admin_dashboard'))

@app.route('/delete_flight/<int:flight_id>')
def delete_flight(flight_id):
    if session.get('role') != 'Admin':
        return redirect(url_for('login'))
        
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM Flight WHERE FlightId=?", (flight_id,))
        conn.commit()
        conn.close()
        flash("Flight deleted successfully!")
    except Exception as e:
        flash(f"Error deleting flight: {e}")
        
    return redirect(url_for('admin_dashboard'))

@app.route('/delete_booking/<ticket_no>')
def delete_booking(ticket_no):
    if session.get('role') != 'Admin':
        return redirect(url_for('login'))
        
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM Bookings WHERE TicketNo=?", (ticket_no,))
        conn.commit()
        conn.close()
        flash("Booking cancelled successfully!")
    except Exception as e:
        flash(f"Error cancelling booking: {e}")
        
    return redirect(url_for('admin_dashboard'))

if __name__ == '__main__':
    app.run(debug=True, port=5000)
