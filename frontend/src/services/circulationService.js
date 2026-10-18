/**
 * BookNest - Circulation API Client (Member 3)
 * Connects frontend React components to the Spring Boot /api/circulation REST endpoints.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Fetch all circulation records from backend.
 * Endpoint: GET /api/circulation
 */
export async function fetchAllCirculation() {
  try {
    const response = await fetch(`${API_BASE_URL}/circulation`, {
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
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}/circulation). Please ensure backend is running.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

/**
 * Fetch active loans (status = 'ISSUED').
 * Endpoint: GET /api/circulation/active
 */
export async function fetchActiveLoans() {
  try {
    const response = await fetch(`${API_BASE_URL}/circulation/active`, {
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
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}/circulation/active).`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

/**
 * Fetch overdue loans.
 * Endpoint: GET /api/circulation/overdue
 */
export async function fetchOverdueLoans() {
  try {
    const response = await fetch(`${API_BASE_URL}/circulation/overdue`, {
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
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}/circulation/overdue).`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

/**
 * Issue a book to a registered member.
 * Endpoint: POST /api/circulation/issue
 * @param {Object} issueData - { bookId, memberId, loanDays }
 */
export async function issueBook(issueData) {
  const payload = {
    bookId: Number(issueData.bookId),
    memberId: Number(issueData.memberId),
    loanDays: issueData.loanDays ? Number(issueData.loanDays) : 14,
  };

  try {
    const response = await fetch(`${API_BASE_URL}/circulation/issue`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      let errorMsg = `Failed to issue book (Status ${response.status})`;
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
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}/circulation/issue. Ensure Spring Boot is running.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

/**
 * Return an issued book.
 * Endpoint: POST /api/circulation/{id}/return
 * @param {number|string} issueId
 * @param {string} [returnDate] - Optional ISO date string (defaults to today)
 */
export async function returnBook(issueId, returnDate = null) {
  const payload = returnDate ? { returnDate } : {};

  try {
    const response = await fetch(`${API_BASE_URL}/circulation/${issueId}/return`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      let errorMsg = `Failed to return book (Status ${response.status})`;
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
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}/circulation/${issueId}/return.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}
