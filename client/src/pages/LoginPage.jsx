import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
    const { login } = useAuth();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await authApi.login(email, password);
            if (response.data.status === 'success') {
                login(response.data.user);
                
                navigate('/courses');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Login failed.');
        }
    };

    return (
        <div className="login-container">
            <div className="card login-card">
                <h2>Sign In</h2>
                {error && <div className="error-message">{error}</div>}
                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Email</label>
                        <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                    </div>
                    <button type="submit" className="btn-primary">Login</button>
                </form>
            </div>
        </div>
    );
}