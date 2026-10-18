/**
 * BookNest - Fines API Client (Member 3)
 * Connects frontend React components to the Spring Boot /api/fines REST endpoints.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Fetch all fines (both settled and pending, including unpersisted active overdue dues).
 * Endpoint: GET /api/fines
 */
export async function fetchAllFines() {
  try {
    const response = await fetch(`${API_BASE_URL}/fines`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    if (!response.ok) {
      let errorMsg = `Server error (Status ${response.status})`;
      try {
        const errorData = await response.json();
        if (errorData?.message) errorMsg = errorData.message;
      } catch {
        // Fallback
      }
      const error = new Error(errorMsg);
      error.status = response.status;
      throw error;
    }

    return await response.json();
  } catch (err) {
    if (err.status) throw err;
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}/fines). Please ensure backend is running.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

/**
 * Fetch fine ledger summary metrics.
 * Endpoint: GET /api/fines/summary
 */
export async function fetchFinesSummary() {
  try {
    const response = await fetch(`${API_BASE_URL}/fines/summary`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' },
    });

    if (!response.ok) {
      let errorMsg = `Server error (Status ${response.status})`;
      try {
        const errorData = await response.json();
        if (errorData?.message) errorMsg = errorData.message;
      } catch {
        // Fallback
      }
      const error = new Error(errorMsg);
      error.status = response.status;
      throw error;
    }

    return await response.json();
  } catch (err) {
    if (err.status) throw err;
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}/fines/summary).`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

/**
 * Collect/pay an overdue fine.
 * Endpoint: POST /api/fines/{id}/pay
 * @param {number|string} fineId
 * @param {string} [paymentNote]
 */
export async function payFine(fineId, paymentNote = '') {
  try {
    const response = await fetch(`${API_BASE_URL}/fines/${fineId}/pay`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify({ paymentNote }),
    });

    if (!response.ok) {
      let errorMsg = `Failed to process fine payment (Status ${response.status})`;
      try {
        const errorDetail = await response.json();
        if (errorDetail?.message) errorMsg = errorDetail.message;
      } catch {
        // Fallback
      }
      const error = new Error(errorMsg);
      error.status = response.status;
      throw error;
    }

    return await response.json();
  } catch (err) {
    if (err.status) throw err;
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}/fines/${fineId}/pay.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}
