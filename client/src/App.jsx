import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import CoursesPage from './pages/CoursesPage';
import ModulesPage from './pages/ModulesPage';
import ChapterDetailsPage from './pages/ChapterDetailsPage'; // Import the new page
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/courses" element={<CoursesPage />} />
        <Route path="/course/:courseId/modules" element={<ModulesPage />} />
        <Route path="/course/:courseId/module/:moduleId/chapter/:chapterId" element={<ChapterDetailsPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;