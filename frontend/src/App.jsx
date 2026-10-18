import React, { useState, useEffect, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import DemoNoticeBanner from './components/DemoNoticeBanner';
import ActionModal from './components/ActionModal';
import Toast from './components/Toast';
import DashboardView from './views/DashboardView';
import BooksView from './views/BooksView';
import MembersView from './views/MembersView';
import CirculationView from './views/CirculationView';
import FinesView from './views/FinesView';

import {
  INITIAL_STATS,
  POPULAR_BOOKS,
} from './data/mockData';
import { fetchAllBooks, fetchBookById, createBook, updateBook, deleteBook } from './services/bookService';
import { fetchAllCirculation, issueBook, returnBook } from './services/circulationService';
import { fetchAllFines, fetchFinesSummary, payFine } from './services/fineService';
import { fetchAllMembers, createMember } from './services/memberService';

import './App.css';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [searchQuery, setSearchQuery] = useState('');
  const [mobileOpen, setMobileOpen] = useState(false);

  // Core application data from MySQL backend
  const [stats, setStats] = useState(INITIAL_STATS);
  const [popularBooks] = useState(POPULAR_BOOKS);
  const [recentActivities, setRecentActivities] = useState([]);

  // Books state
  const [books, setBooks] = useState([]);
  const [loadingBooks, setLoadingBooks] = useState(true);
  const [booksError, setBooksError] = useState(null);

  // Members state
  const [members, setMembers] = useState([]);
  const [loadingMembers, setLoadingMembers] = useState(true);

  // Circulation state (Member 3)
  const [circulation, setCirculation] = useState([]);
  const [loadingCirculation, setLoadingCirculation] = useState(true);
  const [circulationError, setCirculationError] = useState(null);

  // Fines state (Member 3)
  const [fines, setFines] = useState([]);
  const [finesSummary, setFinesSummary] = useState(null);
  const [loadingFines, setLoadingFines] = useState(true);
  const [finesError, setFinesError] = useState(null);

  // Modal & Toast notification states
  const [modalType, setModalType] = useState(null);
  const [modalData, setModalData] = useState(null);
  const [toast, setToast] = useState(null);

  // Auto-dismiss toast notification
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => {
        setToast(null);
      }, 4500);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  // Load books from Spring Boot API and update holding stats
  const loadBooks = useCallback(async () => {
    try {
      setLoadingBooks(true);
      setBooksError(null);
      const data = await fetchAllBooks();
      setBooks(data);

      const totalCopies = data.reduce((sum, b) => sum + (b.totalCopies || 0), 0);
      setStats((prev) =>
        prev.map((s) =>
          s.id === 'total-books'
            ? {
                ...s,
                value: totalCopies.toLocaleString(),
                numericValue: totalCopies,
                subtext: `${data.length} unique titles registered in MySQL`,
              }
            : s
        )
      );
      return data;
    } catch (err) {
      console.error('Failed to load books from backend:', err);
      setBooksError(err.message || 'Cannot reach Spring Boot API.');
      return [];
    } finally {
      setLoadingBooks(false);
    }
  }, []);

  // Load members from Spring Boot API
  const loadMembers = useCallback(async () => {
    try {
      setLoadingMembers(true);
      const data = await fetchAllMembers();
      setMembers(data);
      setStats((prev) =>
        prev.map((s) =>
          s.id === 'registered-members'
            ? {
                ...s,
                value: data.length.toLocaleString(),
                numericValue: data.length,
                subtext: `${data.length} registered accounts in MySQL`,
              }
            : s
        )
      );
    } catch (err) {
      console.warn('Members endpoint unreachable:', err);
      setMembers([]);
    } finally {
      setLoadingMembers(false);
    }
  }, []);

  // Load circulation ledger and update activity & stats
  const loadCirculation = useCallback(async () => {
    try {
      setLoadingCirculation(true);
      setCirculationError(null);
      const data = await fetchAllCirculation();
      setCirculation(data);

      const activeLoans = data.filter((c) => c.status === 'ISSUED' && !c.returnDate);
      const overdueLoans = data.filter((c) => (c.daysOverdue > 0 || c.status === 'Overdue') && c.status !== 'RETURNED');

      // Update Dashboard Stats from real MySQL circulation data
      setStats((prev) =>
        prev.map((s) => {
          if (s.id === 'books-issued') {
            return {
              ...s,
              value: activeLoans.length.toLocaleString(),
              numericValue: activeLoans.length,
              subtext: `${activeLoans.length} books actively checked out`,
            };
          }
          if (s.id === 'overdue-books') {
            return {
              ...s,
              value: overdueLoans.length.toLocaleString(),
              numericValue: overdueLoans.length,
              subtext: `${overdueLoans.length} loans exceeding due date`,
            };
          }
          return s;
        })
      );

      // Convert circulation records to dashboard Recent Activity feed
      const liveActivities = data.slice(0, 10).map((c) => {
          const isReturn = c.status === 'RETURNED' || Boolean(c.returnDate);
          const isOverdue = c.daysOverdue > 0 && !isReturn;
          return {
            id: `act-${c.issueId}`,
            studentName: c.memberName || `Member #${c.memberId}`,
            memberId: c.memberEmail || `ID #${c.memberId}`,
            action: isReturn ? 'Returned Book' : isOverdue ? 'Overdue Loan' : 'Issued Book',
            bookTitle: c.bookTitle || `Book #${c.bookId}`,
            isbn: c.bookAuthor ? `By ${c.bookAuthor}` : `Book #${c.bookId}`,
            timestamp: isReturn ? `Returned ${c.returnDate}` : `Issued ${c.issueDate}`,
            type: isReturn ? 'return' : isOverdue ? 'overdue' : 'issue',
            badgeText: isReturn ? 'Returned' : isOverdue ? `${c.daysOverdue} Days Late` : 'Issued',
            dueDate: c.dueDate,
            issueId: c.issueId,
          };
      });
      setRecentActivities(liveActivities);

      return data;
    } catch (err) {
      console.error('Failed to load circulation records:', err);
      setCirculationError(err.message || 'Cannot reach Circulation API.');
      return [];
    } finally {
      setLoadingCirculation(false);
    }
  }, []);

  // Load fines ledger from backend
  const loadFines = useCallback(async () => {
    try {
      setLoadingFines(true);
      setFinesError(null);
      const [finesList, summaryData] = await Promise.all([
        fetchAllFines(),
        fetchFinesSummary().catch(() => null),
      ]);
      setFines(finesList);
      if (summaryData) setFinesSummary(summaryData);
      return finesList;
    } catch (err) {
      console.error('Failed to load fines:', err);
      setFinesError(err.message || 'Cannot reach Fines API.');
      return [];
    } finally {
      setLoadingFines(false);
    }
  }, []);

  // Load all data on mount and active tab changes
  useEffect(() => {
    let ignore = false;

    async function refreshAll() {
      if (!ignore) {
        await Promise.all([
          loadBooks(),
          loadMembers(),
          loadCirculation(),
          loadFines(),
        ]);
      }
    }

    refreshAll();

    return () => {
      ignore = true;
    };
  }, [activeTab, loadBooks, loadMembers, loadCirculation, loadFines]);

  // Handler: Open Quick Issue Modal
  const handleOpenQuickIssue = (presetData = null) => {
    setModalType('quick-issue');
    setModalData(presetData);
  };

  // Handler: Open Add Book Modal
  const handleOpenAddBook = () => {
    setModalType('add-book');
    setModalData(null);
  };

  // Handler: Open Add Member Modal
  const handleOpenAddMember = () => {
    setModalType('add-member');
    setModalData(null);
  };

  // Handler: Inspect Book
  const handleInspectBook = async (book) => {
    try {
      const freshBook = await fetchBookById(book.id);
      setModalType('inspect-book');
      setModalData(freshBook);
    } catch {
      setModalType('inspect-book');
      setModalData(book);
    }
  };

  // Handler: Open Edit Book Modal
  const handleOpenEditBook = async (book) => {
    try {
      const freshBook = await fetchBookById(book.id);
      setModalType('edit-book');
      setModalData(freshBook);
    } catch {
      setModalType('edit-book');
      setModalData(book);
    }
  };

  // Handler: Open Delete Book Modal
  const handleOpenDeleteBook = (book) => {
    setModalType('delete-book');
    setModalData(book);
  };

  // Handler: Open Collect Fine Modal
  const handleOpenCollectFine = (fine) => {
    setModalType('collect-fine');
    setModalData(fine);
  };

  // Handler: Submit Issue Book (persists to MySQL via POST /api/circulation/issue)
  const handleSubmitIssue = async (formData) => {
    const savedIssue = await issueBook({
      bookId: formData.bookId,
      memberId: formData.memberId,
      loanDays: formData.loanDays || 14,
    });

    // Refresh books (available copies updated), circulation, fines
    await Promise.all([loadBooks(), loadCirculation(), loadFines()]);

    setModalType(null);
    setToast({
      title: 'Book Issued (MySQL)',
      message: `"${savedIssue.bookTitle || `Book #${savedIssue.bookId}`}" successfully issued to ${savedIssue.memberName || `Member #${savedIssue.memberId}`}. Due: ${savedIssue.dueDate}.`,
      type: 'success',
    });
    return savedIssue;
  };

  // Handler: Check-in / Return Book (persists to MySQL via POST /api/circulation/{id}/return)
  const handleReturnBook = async (loan) => {
    try {
      const loanId = loan.issueId || loan.id;
      const result = await returnBook(loanId);

      // Refresh books (available copies restocked), circulation, fines
      await Promise.all([loadBooks(), loadCirculation(), loadFines()]);

      const fineMsg = result.fineAmount > 0
        ? ` Late return (${result.daysOverdue} days overdue). Overdue fine: ₹${result.fineAmount.toFixed(2)} recorded.`
        : ' Returned on time with zero fine.';

      setToast({
        title: 'Book Returned (MySQL)',
        message: `Check-in recorded for "${result.bookTitle || loan.bookTitle}". Shelf inventory restocked.${fineMsg}`,
        type: 'success',
      });
    } catch (err) {
      setToast({
        title: 'Return Failed',
        message: err.message || 'Failed to process return in backend.',
        type: 'error',
      });
    }
  };

  // Handler: Add Book (persists to MySQL via POST /api/books)
  const handleSubmitAddBook = async (bookForm) => {
    const savedBook = await createBook(bookForm);
    await loadBooks();
    setModalType(null);
    setToast({
      title: 'Book Added (MySQL)',
      message: `"${savedBook.title}" (ID #${savedBook.id}) saved to MySQL database.`,
      type: 'success',
    });
    return savedBook;
  };

  // Handler: Update Book (persists to MySQL via PUT /api/books/{id})
  const handleSubmitEditBook = async (bookId, bookForm) => {
    const updatedBook = await updateBook(bookId, bookForm);
    await loadBooks();
    setModalType(null);
    setToast({
      title: 'Book Updated (MySQL)',
      message: `"${updatedBook.title}" (ID #${updatedBook.id}) updated successfully in MySQL database.`,
      type: 'success',
    });
    return updatedBook;
  };

  // Handler: Delete Book (persists to MySQL via DELETE /api/books/{id})
  const handleSubmitDeleteBook = async (bookId) => {
    await deleteBook(bookId);
    await loadBooks();
    const bookTitle = modalData?.title ? `"${modalData.title}"` : `Book #${bookId}`;
    setModalType(null);
    setToast({
      title: 'Book Deleted (MySQL)',
      message: `${bookTitle} was removed from the MySQL catalogue.`,
      type: 'success',
    });
  };

  // Handler: Collect Fine (persists to MySQL via POST /api/fines/{id}/pay)
  const handleSubmitCollectFine = async (fineId, paymentNote) => {
    const settled = await payFine(fineId, paymentNote);

    // Refresh fines ledger and circulation records
    await Promise.all([loadFines(), loadCirculation()]);

    setModalType(null);
    setToast({
      title: 'Fine Collected (MySQL)',
      message: `₹${Number(settled.amount).toFixed(2)} collected from ${settled.memberName || 'patron'}. Payment recorded in MySQL.`,
      type: 'success',
    });
    return settled;
  };

  // Handler: Add Member (persists to MySQL via POST /api/members)
  const handleSubmitAddMember = async (memberForm) => {
    const savedMember = await createMember(memberForm);
    await loadMembers();
    setModalType(null);
    setToast({
      title: 'Member Registered (MySQL)',
      message: `"${savedMember.name}" (ID #${savedMember.id}) registered successfully in MySQL database.`,
      type: 'success',
    });
    return savedMember;
  };

  // Calculate overdue count for sidebar badge
  const overdueCount = fines.filter((f) => f.status === 'Pending' || f.status === 'PENDING').length;

  return (
    <div className="booknest-app-layout">
      {/* Left Sidebar */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        mobileOpen={mobileOpen}
        setMobileOpen={setMobileOpen}
        overdueCount={overdueCount}
      />

      {/* Main Content Area */}
      <div className="main-wrapper">
        <Header
          activeTab={activeTab}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          setMobileOpen={setMobileOpen}
          onOpenQuickIssue={() => handleOpenQuickIssue()}
          onOpenAddBook={handleOpenAddBook}
        />

        <main className="main-content" id="main-content-scroll">
          <DemoNoticeBanner />

          {activeTab === 'dashboard' && (
            <DashboardView
              stats={stats}
              recentActivities={recentActivities}
              popularBooks={popularBooks}
              onNavigateTab={(tab) => setActiveTab(tab)}
              onQuickIssue={() => handleOpenQuickIssue()}
              onAddBook={handleOpenAddBook}
              onQuickReturnDemo={handleReturnBook}
              onInspectBook={handleInspectBook}
              searchQuery={searchQuery}
            />
          )}

          {activeTab === 'books' && (
            <BooksView
              books={books}
              loading={loadingBooks}
              error={booksError}
              onRefresh={loadBooks}
              onAddBook={handleOpenAddBook}
              onInspectBook={handleInspectBook}
              onEditBook={handleOpenEditBook}
              onDeleteBook={handleOpenDeleteBook}
              searchQuery={searchQuery}
              onAddBookSubmit={handleSubmitAddBook}
            />
          )}

          {activeTab === 'members' && (
            <MembersView
              members={members}
              loading={loadingMembers}
              onOpenAddMember={handleOpenAddMember}
              searchQuery={searchQuery}
            />
          )}

          {activeTab === 'circulation' && (
            <CirculationView
              circulation={circulation}
              loading={loadingCirculation}
              error={circulationError}
              onRefresh={loadCirculation}
              onOpenQuickIssue={() => handleOpenQuickIssue()}
              onReturnBook={handleReturnBook}
              searchQuery={searchQuery}
            />
          )}

          {activeTab === 'fines' && (
            <FinesView
              fines={fines}
              summary={finesSummary}
              loading={loadingFines}
              error={finesError}
              onRefresh={loadFines}
              onOpenCollectFine={handleOpenCollectFine}
              searchQuery={searchQuery}
            />
          )}
        </main>
      </div>

      {/* Modal Dialog */}
      {modalType && (
        <ActionModal
          key={modalType}
          modalType={modalType}
          modalData={modalData}
          books={books}
          members={members}
          onClose={() => setModalType(null)}
          onSubmitIssue={handleSubmitIssue}
          onSubmitAddBook={handleSubmitAddBook}
          onSubmitEditBook={handleSubmitEditBook}
          onSubmitDeleteBook={handleSubmitDeleteBook}
          onSubmitCollectFine={handleSubmitCollectFine}
          onSubmitAddMember={handleSubmitAddMember}
          onSwitchToEdit={handleOpenEditBook}
          onSwitchToDelete={handleOpenDeleteBook}
        />
      )}

      {/* Toast Feedback */}
      <Toast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
}
