import { useEffect, useState, useCallback } from "react";
import Layout from "../../components/Layout";
import { useAuth } from "../../context/AuthContext";

import { getMyResidents } from "../../api/residents";
import { getBillsByFlat } from "../../api/maintenance";
import { createOrder, verifyPayment } from "../../api/payments";

export default function MyBills() {
  const { user } = useAuth();

  // -----------------------------
  // State
  // -----------------------------
  const [flats, setFlats] = useState([]);
  const [bills, setBills] = useState([]);

  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(null);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // -----------------------------
  // Load all resident bills
  // -----------------------------
  const loadBills = useCallback(async () => {
    try {
      setLoading(true);

      console.log("Loading resident flats...");

      const residentResponse = await getMyResidents();

      const residentFlats = residentResponse.data ?? [];

      console.log("Resident Flats:", residentFlats);

      setFlats(residentFlats);

      if (residentFlats.length === 0) {
        setBills([]);
        return;
      }

      console.log("Loading maintenance bills...");

      const billResponses = await Promise.all(
        residentFlats.map((flat) =>
          getBillsByFlat(flat.flatNumber)
            .then((response) => response.data ?? [])
        )
      );

      const allBills = billResponses.flat();

      console.log("Bills Loaded:", allBills);

      setBills(allBills);
    } catch (err) {
      console.error(err);

      setError("Failed to load maintenance bills.");
    } finally {
      setLoading(false);
    }
  }, []);

  // -----------------------------
  // Initial Page Load
  // -----------------------------
  useEffect(() => {
    loadBills();
  }, [loadBills]);

  // -----------------------------
  // Debug (Development Only)
  // -----------------------------
  useEffect(() => {
    console.log("========== MyBills ==========");
    console.log("User:", user);
   //cnsole.log("Razorpay SDK:", typeof window.Razorpay);
    console.log("=============================");
  }, [user]);
  // -----------------------------
  // Handle Payment
  // -----------------------------
  async function handlePay(bill) {
    setError("");
    setSuccess("");
    setPaying(bill.id);

    try {
      console.log("========== PAYMENT START ==========");
      console.log("Bill:", bill);

      // 1. Ensure Razorpay SDK is loaded
      if (!window.Razorpay) {
        throw new Error(
          "Razorpay SDK not loaded. Please refresh the page and try again."
        );
      }

      // 2. Read Razorpay Key
      const razorpayKey = import.meta.env.VITE_RAZORPAY_KEY_ID;

      if (!razorpayKey) {
        throw new Error(
          "Razorpay key not configured. Check VITE_RAZORPAY_KEY_ID."
        );
      }

      console.log("Creating Razorpay Order...");

      // 3. Create Order from Backend
      const orderResponse = await createOrder(bill.id);

      const { orderId, amount } = orderResponse.data;

      console.log("Order Created");
      console.log(orderResponse.data);

      // amount already comes in paise
      const options = {
        key: razorpayKey,

        amount,

        currency: "INR",

        order_id: orderId,

        name: "MaintainX",

        description: `Maintenance - Flat ${bill.flatNumber}`,

        theme: {
          color: "#2563eb",
        },

        // ------------------------------------
        // PAYMENT SUCCESS
        // ------------------------------------
        handler: async function (response) {
          try {
            console.log("Payment Successful");
            console.log(response);

            const verifyResponse = await verifyPayment(
              response.razorpay_order_id,
              response.razorpay_payment_id,
              response.razorpay_signature
            );

            console.log("Verify Payment Response:", verifyResponse.data);

            setSuccess(
              `Payment successful for Flat ${bill.flatNumber}.`
            );

            setBills((currentBills) =>
              currentBills.map((currentBill) =>
                currentBill.id === bill.id
                  ? { ...currentBill, paymentStatus: "PAID" }
                  : currentBill
              )
            );

            await loadBills();
          } catch (err) {
            console.error(err);

            setError(
              "Payment verification failed. Please contact the administrator."
            );
          } finally {
            setPaying(null);
          }
        },

        // ------------------------------------
        // USER CLOSED CHECKOUT
        // ------------------------------------
        modal: {
          ondismiss: async function () {
            console.log("Checkout closed by user");

            setPaying(null);

            setError("Payment cancelled.");

            await loadBills();
          },
        },
      };

      const razorpay = new window.Razorpay(options);

      // ------------------------------------
      // PAYMENT FAILED
      // ------------------------------------
      razorpay.on("payment.failed", async function (response) {
        console.error("Payment Failed");

        console.error(response.error);

        setError(
          response.error.description ??
            "Payment failed. Please try again."
        );

        setPaying(null);

        await loadBills();
      });

      console.log("Opening Razorpay Checkout...");

      razorpay.open();
    } catch (err) {
      console.error(err);

      setError(
        err.response?.data?.message ??
          err.message ??
          "Unable to initiate payment."
      );

      setPaying(null);
    } finally {
      console.log("=========== PAYMENT END ===========");
    }
  }  // -----------------------------
  // Derived Data
  // -----------------------------
  const pendingBills = bills.filter(
    (bill) => bill.paymentStatus === "PENDING"
  );

  const paidBills = bills.filter(
    (bill) => bill.paymentStatus === "PAID"
  );

  return (
    <Layout>
      <h2 style={s.heading}>My Maintenance Bills</h2>

      <p style={s.subHeading}>
        View and pay your society maintenance dues.
      </p>

      {error && <div style={s.error}>{error}</div>}

      {success && (
        <div style={s.success}>
          {success}
        </div>
      )}

      {loading && (
        <p style={s.info}>
          Loading maintenance bills...
        </p>
      )}

      {!loading && flats.length === 0 && (
        <div style={s.empty}>
          <p>
            No approved flat registrations found.
          </p>

          <p>
            Submit a join request first.
          </p>
        </div>
      )}

      {!loading && pendingBills.length > 0 && (
        <section style={s.section}>
          <h3 style={s.sectionTitle}>
            Pending Bills ({pendingBills.length})
          </h3>

          {pendingBills.map((bill) => (
            <BillCard
              key={bill.id}
              bill={bill}
              paying={paying}
              onPay={handlePay}
            />
          ))}
        </section>
      )}

      {!loading && paidBills.length > 0 && (
        <section style={s.section}>
          <h3 style={s.sectionTitle}>
            Paid Bills ({paidBills.length})
          </h3>

          {paidBills.map((bill) => (
            <BillCard
              key={bill.id}
              bill={bill}
            />
          ))}
        </section>
      )}
    </Layout>
  );
}

function BillCard({ bill, paying, onPay }) {
  const isPaid = bill.paymentStatus === "PAID";

  return (
    <div style={s.card}>
      <div style={s.left}>

        <div style={s.flat}>
          Flat {bill.flatNumber}
        </div>

        <div style={s.period}>
          {bill.month} {bill.year}
        </div>

        {bill.dueDate && (
          <div style={s.due}>
            Due Date :{" "}
            {new Date(bill.dueDate).toLocaleDateString()}
          </div>
        )}

      </div>

      <div style={s.right}>

        <div style={s.amount}>
          ₹{Number(bill.amount).toLocaleString("en-IN")}
        </div>

        <span
          style={{
            ...s.status,
            ...(isPaid ? s.paid : s.pending),
          }}
        >
          {bill.paymentStatus}
        </span>

        {!isPaid && (
          <button
            style={s.payButton}
            disabled={paying === bill.id}
            onClick={() => onPay(bill)}
          >
            {paying === bill.id
              ? "Processing..."
              : "Pay Now"}
          </button>
        )}

      </div>
    </div>
  );
}

const s = {

  heading: {
    margin: "0 0 4px",
    fontSize: 26,
    fontWeight: 700,
    color: "#0f172a",
  },

  subHeading: {
    marginBottom: 24,
    color: "#64748b",
    fontSize: 14,
  },

  info: {
    color: "#64748b",
  },

  section: {
    marginBottom: 30,
  },

  sectionTitle: {
    fontSize: 18,
    fontWeight: 600,
    marginBottom: 15,
    color: "#1e293b",
  },

  empty: {
    padding: "2rem",
    textAlign: "center",
    border: "2px dashed #e2e8f0",
    borderRadius: 10,
    background: "#fff",
    color: "#64748b",
  },

  error: {
    background: "#fef2f2",
    color: "#dc2626",
    border: "1px solid #fecaca",
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },

  success: {
    background: "#f0fdf4",
    color: "#15803d",
    border: "1px solid #bbf7d0",
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },

  card: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "18px 22px",
    marginBottom: 12,
    background: "#fff",
    borderRadius: 10,
    boxShadow: "0 2px 6px rgba(0,0,0,0.08)",
  },

  left: {
    display: "flex",
    flexDirection: "column",
    gap: 5,
  },

  right: {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-end",
    gap: 8,
  },

  flat: {
    fontSize: 18,
    fontWeight: 600,
    color: "#0f172a",
  },

  period: {
    color: "#64748b",
    fontSize: 13,
  },

  due: {
    color: "#94a3b8",
    fontSize: 12,
  },

  amount: {
    fontSize: 22,
    fontWeight: 700,
    color: "#0f172a",
  },

  status: {
    padding: "3px 12px",
    borderRadius: 20,
    fontWeight: 600,
    fontSize: 11,
  },

  paid: {
    background: "#d1fae5",
    color: "#065f46",
  },

  pending: {
    background: "#fef3c7",
    color: "#92400e",
  },

  payButton: {
    background: "#2563eb",
    color: "#fff",
    border: "none",
    borderRadius: 6,
    padding: "8px 18px",
    cursor: "pointer",
    fontWeight: 600,
    fontSize: 13,
  },

};