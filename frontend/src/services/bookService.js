/**
 * BookNest - Books API Client
 * Connects frontend React components to the Spring Boot backend REST API.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Fetch all books from the backend.
 * Endpoint: GET /api/books
 * @returns {Promise<Array>} List of books from MySQL
 */
export async function fetchAllBooks() {
  try {
    const response = await fetch(`${API_BASE_URL}/books`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      let errorMsg = `Server error (Status ${response.status})`;
      try {
        const errorData = await response.json();
        if (errorData?.message) errorMsg = errorData.message;
      } catch {
        // Fallback if not JSON
      }
      const error = new Error(errorMsg);
      error.status = response.status;
      throw error;
    }

    return await response.json();
  } catch (err) {
    if (err.status) {
      throw err;
    }
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}). Please ensure the backend is running.`);
    connErr.isNetworkError = true;
    connErr.original = err;
    throw connErr;
  }
}

/**
 * Fetch a single book by ID.
 * Endpoint: GET /api/books/{id}
 * @param {number|string} id - Book ID
 * @returns {Promise<Object>} Book object
 */
export async function fetchBookById(id) {
  try {
    const response = await fetch(`${API_BASE_URL}/books/${id}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      let errorMsg = `Book #${id} not found`;
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
    if (err.status) {
      throw err;
    }
    const connErr = new Error(`Cannot connect to Spring Boot backend (${API_BASE_URL}). Please ensure the backend is running.`);
    connErr.isNetworkError = true;
    connErr.original = err;
    throw connErr;
  }
}

/**
 * Add a new book to the database.
 * Endpoint: POST /api/books
 * @param {Object} bookData - { id, title, author, totalCopies, availableCopies }
 * @returns {Promise<Object>} Created book
 */
export async function createBook(bookData) {
  const payload = {
    id: Number(bookData.id),
    title: bookData.title ? bookData.title.trim() : '',
    author: bookData.author ? bookData.author.trim() : '',
    totalCopies: Number(bookData.totalCopies),
    availableCopies: bookData.availableCopies !== undefined && bookData.availableCopies !== ''
      ? Number(bookData.availableCopies)
      : Number(bookData.totalCopies),
  };

  try {
    const response = await fetch(`${API_BASE_URL}/books`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      let errorMsg = `Failed to add book (Status ${response.status})`;
      let errorDetail = null;
      try {
        errorDetail = await response.json();
        if (errorDetail?.message) {
          errorMsg = errorDetail.message;
        }
      } catch {
        // Fallback
      }

      const error = new Error(errorMsg);
      error.status = response.status;
      error.data = errorDetail;
      throw error;
    }

    return await response.json();
  } catch (err) {
    if (err.status) {
      throw err;
    }
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}. Ensure Spring Boot is running with active MySQL connection.`);
    connErr.isNetworkError = true;
    connErr.original = err;
    throw connErr;
  }
}

/**
 * Update an existing book in the database.
 * Endpoint: PUT /api/books/{id}
 * @param {number|string} id - Book ID
 * @param {Object} bookData - { title, author, totalCopies, availableCopies }
 * @returns {Promise<Object>} Updated book
 */
export async function updateBook(id, bookData) {
  const payload = {
    id: Number(id),
    title: bookData.title ? bookData.title.trim() : '',
    author: bookData.author ? bookData.author.trim() : '',
    totalCopies: Number(bookData.totalCopies),
    availableCopies: bookData.availableCopies !== undefined && bookData.availableCopies !== ''
      ? Number(bookData.availableCopies)
      : Number(bookData.totalCopies),
  };

  try {
    const response = await fetch(`${API_BASE_URL}/books/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      let errorMsg = `Failed to update book (Status ${response.status})`;
      let errorDetail = null;
      try {
        errorDetail = await response.json();
        if (errorDetail?.message) {
          errorMsg = errorDetail.message;
        }
      } catch {
        // Fallback
      }

      const error = new Error(errorMsg);
      error.status = response.status;
      error.data = errorDetail;
      throw error;
    }

    return await response.json();
  } catch (err) {
    if (err.status) {
      throw err;
    }
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}. Ensure Spring Boot is running with active MySQL connection.`);
    connErr.isNetworkError = true;
    connErr.original = err;
    throw connErr;
  }
}

/**
 * Delete a book by ID from the database.
 * Endpoint: DELETE /api/books/{id}
 * @param {number|string} id - Book ID
 * @returns {Promise<Object>} Deletion result
 */
export async function deleteBook(id) {
  try {
    const response = await fetch(`${API_BASE_URL}/books/${id}`, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      let errorMsg = `Failed to delete book (Status ${response.status})`;
      try {
        const errorDetail = await response.json();
        if (errorDetail?.message) {
          errorMsg = errorDetail.message;
        }
      } catch {
        // Fallback
      }

      const error = new Error(errorMsg);
      error.status = response.status;
      throw error;
    }

    if (response.status === 204) {
      return { success: true, id };
    }

    try {
      return await response.json();
    } catch {
      return { success: true, id };
    }
  } catch (err) {
    if (err.status) {
      throw err;
    }
    const connErr = new Error(`Cannot reach backend at ${API_BASE_URL}. Ensure Spring Boot is running with active MySQL connection.`);
    connErr.isNetworkError = true;
    connErr.original = err;
    throw connErr;
  }
}

/**
 * Check backend health status.
 * Endpoint: GET /api/health
 * @returns {Promise<Object>} { status: 'UP', service: '...', timestamp: '...' }
 */
export async function checkBackendHealth() {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, {
      headers: { 'Accept': 'application/json' },
    });
    if (!response.ok) {
      throw new Error(`Health check returned status ${response.status}`);
    }
    return await response.json();
  } catch (err) {
    if (err.status) throw err;
    const connErr = new Error(`Health check unreachable at ${API_BASE_URL}/health.`);
    connErr.isNetworkError = true;
    throw connErr;
  }
}
