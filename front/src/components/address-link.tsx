interface AddressLinkProps {
  address: string;
  type: '도로명' | '지번';
}

export function AddressLink({ address, type }: AddressLinkProps) {
  const naverMapUrl = `https://map.naver.com/p/search/${encodeURIComponent(address)}/address`;

  return (
    <div className="flex items-start gap-2 py-1">
      <span className="min-w-[48px] text-xs font-medium text-gray-500">{type}</span>
      <a
        href={naverMapUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="text-sm text-blue-600 hover:text-blue-800 hover:underline"
      >
        {address}
      </a>
    </div>
  );
}
