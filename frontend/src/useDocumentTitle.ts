import { useEffect } from "react";

const SITE = "Thief and Police";

// প্রতিটা পেজ/phase-এর নিজস্ব ট্যাব শিরোনাম (স্ক্রিন রিডার ব্যবহারকারীরা পেজ বদল বুঝতে এটা শোনে)
export function useDocumentTitle(page: string) {
  useEffect(() => {
    document.title = page ? `${page} | ${SITE}` : SITE;
  }, [page]);
}
