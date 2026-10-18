/**
 * BookNest - Members API Client
 * Connects frontend to the Spring Boot /api/members endpoints to enable
 * selection of registered patrons when issuing books.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export async function fetchAllMembers() {
  try {
    const response = await fetch(`${API_BASE_URL}/members`, {
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
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}/members).`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}

export async function createMember(memberData) {
  try {
    const response = await fetch(`${API_BASE_URL}/members`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify(memberData),
    });

    if (!response.ok) {
      let errorMsg = `Failed to add member (Status ${response.status})`;
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
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}/members.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}
