<script>
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    let { data } = $props();

    onMount(() => {
        // Immer zum Dashboard weiterleiten
        if (data.isAuthenticated) {
            goto("/dashboard");
        } else {
            goto("/login");
        }
    });
</script>

<div class="loading-wrapper">
    <div class="spinner"></div>
    <p>Weiterleitung...</p>
</div>

<style>
    .loading-wrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100vh;
        gap: 1rem;
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #e8e0f0;
        border-top-color: #2F124D;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }

    p {
        color: #6b647a;
        font-size: 0.9rem;
    }
</style>
